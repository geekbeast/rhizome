keytool -keystore rhizome.jks -delete -alias token
keytool -genkeypair -keyalg EC -keysize 256 -keystore rhizome.jks -alias token -keypass rhizome -dname "CN=Rhizome, OU=SLG, O=Geekbeast, L=Menlo Park, S=CA, C=US" -validity 365
keytool -keystore rhizome.jks -exportcert -alias token -file token.cer
