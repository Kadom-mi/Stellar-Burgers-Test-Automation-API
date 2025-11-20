import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import net.datafaker.Faker;
import org.junit.After;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

public class UserCreateTest {
    private static final Faker faker = new Faker();
    private String accessToken;

    private User createUser() {
        User user = new User();
        user.setEmail(faker.internet().safeEmailAddress());
        user.setName(faker.name().fullName());
        user.setPassword(faker.internet().password(8, 16));
        return user;
    }

    @Test
    @DisplayName("Cоздание пользователя")
    @Description("Создание пользователя со всеми уникальными полями - Ожидание 200")
    public void testCreateUser() {
        User user = createUser();

        Response response = UserMethods.createUser(user);
        String accessToken = response.then().extract().path("accessToken").toString();

        response.then().log().all()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("accessToken", notNullValue())
                .body("user.email", equalTo(user.getEmail()))
                .body("user.name", equalTo(user.getName()));
    }

    @Test
    @DisplayName("Cоздание пользователя, который уже зарегистрирован")
    @Description("Создание пользователя, который уже зарегистрирован - Ожидание 403")
    public void createDoubleUser() {
        User user = createUser();

        Response createResponse = UserMethods.createUser(user);
        String accessToken = createResponse.then().extract().path("accessToken").toString();

        createResponse.then()
                .statusCode(200)
                .body("success", equalTo(true));

        Response duplicateResponse = UserMethods.createUser(user);

        duplicateResponse.then().log().all()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));
    }

    @Test
    @DisplayName("Cоздание пользователя без поля E-mail")
    @Description("Cоздание пользователя без поля E-mail - Ожидание 403")
    public void createUserWithoutEmail() {
        User user = new User();
        user.setName(faker.name().fullName());
        user.setEmail("");
        user.setPassword(faker.internet().password(8, 16));

        Response response = UserMethods.createUser(user);

        response.then().log().all()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Cоздание пользователя без поля Password")
    @Description("Cоздание пользователя без поля Password - Ожидание 403")
    public void createUserWithoutPassword() {
        User user = new User();
        user.setName(faker.name().fullName());
        user.setEmail(faker.internet().safeEmailAddress());
        user.setPassword("");

        Response response = UserMethods.createUser(user);

        response.then().log().all()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Cоздание пользователя без поля Name")
    @Description("Cоздание пользователя без поля Name - Ожидание 403")
    public void createUserWithoutName() {
        User user = new User();
        user.setName("");
        user.setEmail(faker.internet().safeEmailAddress());
        user.setPassword(faker.internet().password(8, 16));

        Response response = UserMethods.createUser(user);

        response.then().log().all()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @After
    public void cleanupUser() {
        if (accessToken != null && !accessToken.isEmpty()) {
            try {
                UserMethods.deleteUser(accessToken);
            } catch (Exception e) {
                System.err.println("Cleanup failed: " + e.getMessage());
            }
        }
    }
}
