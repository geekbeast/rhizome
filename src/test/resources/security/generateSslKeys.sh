read -p "Enter subject alternative name[localhost]: " san
if [ $san = "" ]; then
    san='localhost'
fi
echo "Using $san"
keytool -keystore rhizome.jks -delete -alias ssl
keytool -genkeypair -keyalg RSA -keysize 4096 -keystore rhizome.jks -alias ssl -keypass rhizome -dname "CN=$san, OU=SLG, O=Geekbeast, L=Menlo Park, S=CA, C=US" -validity 365 
keytool -keystore rhizome.jks -exportcert -alias ssl -file ssl.cer
