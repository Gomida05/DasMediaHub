# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class com.das.forui.** { *; }

# Keep Firebase services
-keep class com.google.firebase.** { *; }

# Keep your Python code if you're using Chaquopy
-keep class com.chaquo.python.** { *; }

-keep class com.google.gson.** { *; }
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

-dontwarn com.das.forui.MainApplication$Youtuber


-dontwarn com.android.org.conscrypt.SSLParametersImpl
-dontwarn com.sun.net.ssl.KeyManager
-dontwarn com.sun.net.ssl.TrustManager
-dontwarn javax.naming.NamingEnumeration
-dontwarn javax.naming.NamingException
-dontwarn javax.naming.directory.Attribute
-dontwarn javax.naming.directory.Attributes
-dontwarn javax.naming.directory.DirContext
-dontwarn javax.naming.directory.InitialDirContext
-dontwarn javax.naming.directory.SearchControls
-dontwarn javax.naming.directory.SearchResult
-dontwarn org.apache.harmony.xnet.provider.jsse.SSLParametersImpl
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn sun.misc.JavaNetAccess
-dontwarn sun.misc.SharedSecrets
-dontwarn sun.misc.VM
-dontwarn sun.net.util.IPAddressUtil
-dontwarn sun.net.www.http.HttpClient
-dontwarn sun.net.www.http.KeepAliveCache
-dontwarn sun.net.www.protocol.http.Handler
-dontwarn sun.net.www.protocol.http.HttpURLConnection$TunnelState
-dontwarn sun.net.www.protocol.http.HttpURLConnection
-dontwarn sun.net.www.protocol.https.Handler
-dontwarn sun.security.action.GetBooleanAction
-dontwarn sun.security.action.GetIntegerAction
-dontwarn sun.security.action.GetLongAction
-dontwarn sun.security.action.GetPropertyAction
-dontwarn sun.security.action.OpenFileInputStreamAction
-dontwarn sun.security.internal.interfaces.TlsMasterSecret
-dontwarn sun.security.internal.spec.TlsKeyMaterialParameterSpec
-dontwarn sun.security.internal.spec.TlsKeyMaterialSpec
-dontwarn sun.security.internal.spec.TlsPrfParameterSpec
-dontwarn sun.security.internal.spec.TlsRsaPremasterSecretParameterSpec
-dontwarn sun.security.jca.JCAUtil
-dontwarn sun.security.jca.ProviderList
-dontwarn sun.security.jca.Providers
-dontwarn sun.security.pkcs.PKCS8Key
-dontwarn sun.security.provider.certpath.AlgorithmChecker
-dontwarn sun.security.provider.certpath.CertId
-dontwarn sun.security.provider.certpath.OCSP
-dontwarn sun.security.provider.certpath.OCSPResponse$ResponseStatus
-dontwarn sun.security.provider.certpath.OCSPResponse$SingleResponse
-dontwarn sun.security.provider.certpath.OCSPResponse
-dontwarn sun.security.provider.certpath.PKIXExtendedParameters
-dontwarn sun.security.provider.certpath.ResponderId
-dontwarn sun.security.provider.certpath.UntrustedChecker
-dontwarn sun.security.util.AlgorithmDecomposer
-dontwarn sun.security.util.AnchorCertificates
-dontwarn sun.security.util.BitArray
-dontwarn sun.security.util.Cache$CacheVisitor
-dontwarn sun.security.util.Cache
-dontwarn sun.security.util.Debug
-dontwarn sun.security.util.DerInputStream
-dontwarn sun.security.util.DerOutputStream
-dontwarn sun.security.util.DerValue
-dontwarn sun.security.util.DisabledAlgorithmConstraints
-dontwarn sun.security.util.ECUtil
-dontwarn sun.security.util.HostnameChecker
-dontwarn sun.security.util.KeyUtil
-dontwarn sun.security.util.LegacyAlgorithmConstraints
-dontwarn sun.security.util.ObjectIdentifier
-dontwarn sun.security.validator.ValidatorException
-dontwarn sun.security.x509.AlgorithmId
-dontwarn sun.security.x509.CertificateExtensions
-dontwarn sun.security.x509.Extension
-dontwarn sun.security.x509.KeyIdentifier
-dontwarn sun.security.x509.NetscapeCertTypeExtension
-dontwarn sun.security.x509.PKIXExtensions
-dontwarn sun.security.x509.SerialNumber
-dontwarn sun.security.x509.X500Name
-dontwarn sun.security.x509.X509CertImpl
-dontwarn sun.security.x509.X509CertInfo
-dontwarn sun.security.x509.X509Key
-dontwarn sun.util.logging.PlatformLogger$Level
-dontwarn sun.util.logging.PlatformLogger
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE

