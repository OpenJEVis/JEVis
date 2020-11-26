package org.jevis.jeopc;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.RSAPublicKeyStructure;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class KeyGenerator {

    public static final String alias = "OPC-Driver";
    private PrivateKey privKey;
    private PublicKey pubKey;
    private SecureRandom secureRandom = new SecureRandom();
    KeyPair certKeyPair;

    final static class GeneratedCert {
        public final PrivateKey privateKey;
        public final X509Certificate certificate;

        public GeneratedCert(PrivateKey privateKey, X509Certificate certificate) {
            this.privateKey = privateKey;
            this.certificate = certificate;
        }
    }

    private GeneratedCert generatedCert;

    public KeyGenerator() {


    }

    public PrivateKey getPrivKey() {
        return privKey;
    }

    public PublicKey getPubKey() {
        return pubKey;
    }

    public KeyPair getKeyPair() {
        if (certKeyPair != null) {
            return certKeyPair;
        }
        return new KeyPair(pubKey, privKey);
    }

    public GeneratedCert getGeneratedCert() {
        return generatedCert;
    }


    public void create(String cnName, String domain, boolean isCA) throws NoSuchAlgorithmException, CertificateException, OperatorCreationException, CertIOException {
        // Generate the key-pair with the official Java API's
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        certKeyPair = keyGen.generateKeyPair();
        X500Name name = new X500Name("CN=" + alias);
        // If you issue more than just test certificates, you might want a decent serial number schema ^.^
        BigInteger serialNumber = BigInteger.valueOf(System.currentTimeMillis());
        Instant validFrom = Instant.now();
        Instant validUntil = validFrom.plus(10 * 360, ChronoUnit.DAYS);

        // If there is no issuer, we self-sign our certificate.
        X500Name issuerName;
        PrivateKey issuerKey;
        issuerName = name;
        issuerKey = certKeyPair.getPrivate();

        // The cert builder to build up our certificate information
        JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(
                issuerName,
                serialNumber,
                Date.from(validFrom), Date.from(validUntil),
                name, certKeyPair.getPublic());

        // Make the cert to a Cert Authority to sign more certs when needed
        /**
         if (isCA) {
         builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(isCA));
         }
         **/
        // Modern browsers demand the DNS name entry
        if (domain != null) {
            builder.addExtension(Extension.subjectAlternativeName, false,
                    new GeneralNames(new GeneralName(GeneralName.dNSName, domain)));
        }

        // Finally, sign the certificate:
        ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA").build(issuerKey);
        X509CertificateHolder certHolder = builder.build(signer);
        X509Certificate cert = new JcaX509CertificateConverter().getCertificate(certHolder);

        generatedCert = new GeneratedCert(certKeyPair.getPrivate(), cert);
    }


    public void create1() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024, secureRandom);
        KeyPair keypair = keyGen.generateKeyPair();
        privKey = keypair.getPrivate();
        pubKey = keypair.getPublic();
    }

    public void crete2() throws NoSuchAlgorithmException, InvalidKeySpecException {
        RSAKeyPairGenerator gen = new RSAKeyPairGenerator();
        gen.init(new RSAKeyGenerationParameters(BigInteger.valueOf(3), secureRandom, 1024, 80));
        AsymmetricCipherKeyPair keypair = gen.generateKeyPair();
        RSAKeyParameters publicKey = (RSAKeyParameters) keypair.getPublic();
        RSAPrivateCrtKeyParameters privateKey = (RSAPrivateCrtKeyParameters) keypair.getPrivate();
        // used to get proper encoding for the certificate
        RSAPublicKeyStructure pkStruct = new RSAPublicKeyStructure(publicKey.getModulus(), publicKey.getExponent());
        // JCE format needed for the certificate - because getEncoded() is necessary…
        pubKey = KeyFactory.getInstance("RSA").generatePublic(
                new RSAPublicKeySpec(publicKey.getModulus(), publicKey.getExponent()));
        // and this one for the KeyStore
        privKey = KeyFactory.getInstance("RSA").generatePrivate(
                new RSAPrivateCrtKeySpec(publicKey.getModulus(), publicKey.getExponent(),
                        privateKey.getExponent(), privateKey.getP(), privateKey.getQ(),
                        privateKey.getDP(), privateKey.getDQ(), privateKey.getQInv()));
    }
}
