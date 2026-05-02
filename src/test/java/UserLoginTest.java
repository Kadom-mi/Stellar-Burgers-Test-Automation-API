import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import net.datafaker.Faker;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

public class UserLoginTest {
    private static final Faker faker = new Faker();

    private User user;
    private String accessToken;

    @Before
    public void setUp() {
        user = new User();
        user.setEmail(faker.internet().safeEmailAddress());
        user.setName(faker.name().fullName());
        user.setPassword(faker.internet().password(8, 16));
        accessToken = null;

        Response createResponse = UserMethods.createUser(user);
        createResponse.then()
                .statusCode(200)
                .body("success", equalTo(true));

        accessToken = createResponse.then().extract().path("accessToken").toString();
    }

    @Test
    @DisplayName("Успешная авторизация")
    @Description("Успешная авторизация - Ожидание 200")
    public void loginSuccessUserExists() {
        Response loginResponse = UserMethods.loginUser(user);

        loginResponse.then().log().all()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue())
                .body("user.email", equalTo(user.getEmail()));
    }

    @Test
    @DisplayName("Авторизация с пустым email")
    @Description("Авторизация с пустым email - Ожидание 401")
    public void loginFailureEmptyEmail() {
        User user = new User();
        user.setEmail("");
        user.setPassword("password123");
        user.setName(faker.name().fullName());

        Response response = UserMethods.loginUser(user);

        response.then().log().all()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @Test
    @DisplayName("Авторизация с пустым паролем")
    @Description("Авторизация с пустым паролем - Ожидание 401")
    public void loginFailureEmptyPassword() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("");
        user.setName(faker.name().fullName());

        Response response = UserMethods.loginUser(user);

        response.then().log().all()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @Test
    @DisplayName("Авторизация с неверным email")
    @Description("Авторизация с неверным email - Ожидание 401")
    public void loginFailureUserNotExists() {
        User user = new User();
        user.setEmail("nonexistent@example.com");
        user.setPassword("password123");
        user.setName(faker.name().fullName());

        Response response = UserMethods.loginUser(user);

        response.then().log().all()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @Test
    @DisplayName("Авторизация с неверным паролем")
    @Description("Авторизация с неверным паролем - Ожидание 401")
    public void loginFailurePasswordIncorrect() {
        User loginUserObj = new User();
        loginUserObj.setEmail(user.getEmail());
        loginUserObj.setPassword(user.getPassword() + "_wrong");
        loginUserObj.setName(user.getName());

        Response response = UserMethods.loginUser(loginUserObj);

        response.then().log().all()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @After
    public void tearDown() {
        if (accessToken != null && !accessToken.isEmpty()) {
            try {
                UserMethods.deleteUser(accessToken);
            } catch (Exception e) {
                System.err.println("Cleanup failed for token " + accessToken + ": " + e.getMessage());
            }
        }
    }
}
