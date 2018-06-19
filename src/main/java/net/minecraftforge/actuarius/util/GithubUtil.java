package net.minecraftforge.actuarius.util;

import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.HttpConnector;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.minecraftforge.actuarius.Main;
import reactor.util.annotation.Nullable;

public class GithubUtil {
    
    private static @Nullable GitHub github;

    private static @Nullable String token;
    @SuppressWarnings("null")
    private static Instant tokenExpiry = Instant.EPOCH;

    public static synchronized GitHub getClient() {
        if (github == null) {
            try {
                github = new GitHubBuilder().withConnector(new HttpConnector() {

                    @Override
                    public HttpURLConnection connect(URL url) throws IOException {
                        HttpURLConnection ret = (HttpURLConnection) url.openConnection();

                        ret.setRequestProperty("Authorization", "token " + getToken());
                        ret.setRequestProperty("Accept", "application/vnd.github.machine-man-preview+json");
                        return ret;
                    }
                }).build();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return github;
    }
    
    static synchronized String getToken() {

        if (token == null || tokenExpiry.isBefore(Instant.now())) {

            RSAPrivateKey key = getPrivateKey(Main.config.get("discord.private_key"));

            Algorithm algorithm = Algorithm.RSA256(null, key);
            Date now = new Date();

            String jwt = JWT.create()
                    .withIssuer(Main.config.<Integer>get("discord.app_id").toString())
                    .withIssuedAt(now)
                    // Use 9 minutes here, because 10 minutes is the limit and 
                    // may be too long if local time is ahead of server time.
                    .withExpiresAt(new Date(now.getTime() + TimeUnit.MINUTES.toMillis(9))) 
                    .sign(algorithm);

            try {
                HttpURLConnection jwtPingRequest = (HttpURLConnection) new URL("https://api.github.com/app").openConnection();

                // Update the JWT to re-authenticate the app
                jwtPingRequest.setRequestProperty("Authorization", "Bearer " + jwt);
                jwtPingRequest.setRequestProperty("Accept", "application/vnd.github.machine-man-preview+json");
                jwtPingRequest.getInputStream().close();
                
                // Request a new token with this authenticated JWT
                HttpURLConnection tokenRequest = (HttpURLConnection) new URL("https://api.github.com/installations/" + Main.config.<Integer>get("discord.installation_id") + "/access_tokens").openConnection();
                tokenRequest.setRequestMethod("POST");
                tokenRequest.setRequestProperty("Authorization", "Bearer " + jwt);
                tokenRequest.setRequestProperty("Accept", "application/vnd.github.machine-man-preview+json");

                // Read the result
                ObjectMapper jsonParser = new ObjectMapper();
                JsonNode jsonObject = jsonParser.readTree(tokenRequest.getInputStream());

                // Note the expiry so re-auth is done as little as possible
                token = jsonObject.get("token").asText();
                tokenExpiry = Instant.parse(jsonObject.get("expires_at").asText());

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        
        return token;
    }
    
    static RSAPrivateKey getPrivateKey(String pemFile) {
        try (PemReader reader = new PemReader(new FileReader(pemFile))) {
            PemObject pem = reader.readPemObject();
        
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(pem.getContent());
            // "BC" forces the BouncyCastle version, which is required when using the BouncyCastle PemObject
            KeyFactory kf = KeyFactory.getInstance("RSA", "BC");
            return (RSAPrivateKey) kf.generatePrivate(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException | NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
    }
}
