package at.qe.skeleton.tests.configs;

import at.qe.skeleton.configs.JwtConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "app.jwt.secret=myVeryLongSecretKeyForTestingHS512Compliance12345678901234567890",
        "app.jwt.expirationMs=3600000",
        "app.jwt.login.url=/api/login",
        "app.jwt.token.header=Authorization",
        "app.jwt.token.prefix=Bearer ",
        "app.jwt.token.type=JWT",
        "app.jwt.token.issuer=test-issuer",
        "app.jwt.token.audience=test-audience"
})
class JwtConfigTest {

    @Autowired
    private JwtConfig jwtConfig;

    @Test
    void testJwtPropertyBinding() {
        // here we are only testing for the correct injection. Not the setters.
        Assertions.assertEquals("myVeryLongSecretKeyForTestingHS512Compliance12345678901234567890", jwtConfig.getJwtSecret());
        Assertions.assertEquals(3600000L, jwtConfig.getJwtExpirationMs());
        Assertions.assertEquals("/api/login", jwtConfig.getLoginUrl());
        Assertions.assertEquals("Authorization", jwtConfig.getTokenHeader());
        Assertions.assertEquals("Bearer ", jwtConfig.getTokenPrefix());
        Assertions.assertEquals("JWT", jwtConfig.getTokenType());
        Assertions.assertEquals("test-issuer", jwtConfig.getTokenIssuer());
        Assertions.assertEquals("test-audience", jwtConfig.getTokenAudience());
    }
}