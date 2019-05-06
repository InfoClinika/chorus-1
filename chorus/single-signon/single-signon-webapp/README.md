This is a Single Sign-On application which goal is to link Chorus and Panorama accounts and let user access both systems using single authentication.
Based on https://github.com/Jasig/cas-overlay-template which is recommended way to use CAS.

To deploy application locally:
- mvn clean package
- turn on SSL for the servlet container you are using
        - (uncomment and modify rows on conf/server.xml)
            <Connector SSLEnabled="true" acceptCount="100" clientAuth="false"
                           disableUploadTimeout="true" enableLookups="false" maxThreads="150"
                           port="8443" keystoreFile="c:\Users\andrii.loboda\.keystore" keystorePass="password"
                           protocol="org.apache.coyote.http11.Http11Protocol" scheme="https"
                           secure="true" sslProtocol="TLS" />
        - create fake .keystore file using command(from JAVA_HOME/bin folder):
            keytool -genkey -alias tomcat -keyalg RSA
            Notice that .keystore generated at user home folder
- Create cas-custom.properties in user.home folder with correct path to Chorus and Panorama authentication endpoints.
- Deploy this module to servlet container.

To deploy the application on amazon instance:
- mvn clean package
- turn on SSL for the servlet container you are using
        - (uncomment and modify rows on conf/server.xml)
            <Connector SSLEnabled="true" acceptCount="100" clientAuth="false"
                           disableUploadTimeout="true" enableLookups="false" maxThreads="150"
                           port="8443" keystoreFile="c:\Users\andrii.loboda\.keystore" keystorePass="password"
                           protocol="org.apache.coyote.http11.Http11Protocol" scheme="https"
                           secure="true" sslProtocol="TLS" />
        - create fake .keystore file using command(from JAVA_HOME/bin folder):
            keytool -genkey -alias tomcat -keyalg RSA
            Notice that .keystore generated at user home folder
- Create cas-custom.properties in user.home folder with:
        - correct path to Chorus and Panorama authentication endpoints(i.e. chorus.auth.service.url=https://dev.chorusproject.org/rest/authService)
- Deploy this module to servlet container with the same name("sso.war")

To integrate the application with Chorus webapp
- Go to instance with Chorus webapp and create cas.properties at home folder with
        - cas.service.host={PUT CHORUS WEBAPP URL, i.e. cas.service.host=dev.chorusproject.org}
        - cas.server.host={PUT SSO WEBAPP URL, i.e. cas.server.host=dev.chorusproject.org/sso}

Tips and tricks for nginx settings to make Chorus work with CAS:
- add location for your public domain(i.e. dev.chorusproject.org):
     location /sso {

                   proxy_pass https://CHORUS_SSO_WEBAPP:8443/sso;
                   proxy_redirect     off;
                   proxy_set_header   Host             $host;
                   proxy_set_header   X-Real-IP        $remote_addr;
                   proxy_set_header   X-Forwarded-For  $proxy_add_x_forwarded_for;
        }
