(ns leiningen.aws-ecr-auth
  (:require [cognitect.aws.client.api :as aws]
            [cognitect.aws.credentials :as credentials]
            [clojure.string :as str]
            [leiningen.core.main :as lein]
            [cognitect.aws.util])
  (:import [java.util Base64]
           (java.io ByteArrayInputStream)))

(defn decode-base64 [to-decode]
  (String. (.decode (Base64/getDecoder) to-decode)))

(defn assumed-role-credentials-provider [role-arn session-name refresh-every-n-seconds]
  (credentials/auto-refreshing-credentials
    (reify credentials/CredentialsProvider
      (fetch [_]
        ;; The following is a workaround for a XML-parsing issue when using
        ;; the older clojure.data.xml bundled with Leiningen
        (with-redefs [cognitect.aws.util/xml-read
                      (fn [s] (clojure.data.xml/parse (ByteArrayInputStream. (.getBytes ^String s "UTF-8"))
                                                      :namespace-aware false))]

         (let [sts (aws/client {:api :sts})
               sts-request {:op      :AssumeRole
                            :request {:RoleArn         role-arn
                                      :RoleSessionName session-name}}
               sts-response (aws/invoke sts sts-request)]
           (lein/info "sts-resp" sts-response)
           (if-let [creds (:Credentials sts-response)]
             {:aws/access-key-id     (:AccessKeyId creds)
              :aws/secret-access-key (:SecretAccessKey creds)
              :aws/session-token     (:SessionToken creds)
              ::credentials/ttl      refresh-every-n-seconds}
             (throw (ex-info (str "Unable to gain STS temporary credentials:" sts-response) sts-response)))))))))


(defn get-client [api credentials-config]
  (if-let [crp (case (:type credentials-config)
                 :assume-role
                 (assumed-role-credentials-provider (:role-arn credentials-config) "session" 600)

                 :access-key
                 (credentials/basic-credentials-provider (select-keys credentials-config [:access-key-id :secret-access-key]))

                 :system-properties
                 (credentials/system-property-credentials-provider)

                 :profile
                 (credentials/profile-credentials-provider (:profile-name credentials-config))

                 :environment
                 (credentials/environment-credentials-provider))]
    (aws/client {:api api :credentials-provider crp})
    (aws/client {:api api})))

(defn ecr-auth [credentials-config]
  (lein/debug "Generating ECR authorization from" (str credentials-config))
  (let [ecr-response (aws/invoke (get-client :ecr credentials-config)
                                 {:op :GetAuthorizationToken})]
    (if-let [authz (get-in ecr-response
                           [:authorizationData 0])]
      (let [[user pass] (str/split (decode-base64 (:authorizationToken authz)) #":")]
        {:username user
         :password pass
         :endpoint (:proxyEndpoint authz)})
      (throw (ex-info (str "Cannot generate ECR authorization credentials: " ecr-response) ecr-response)))))

