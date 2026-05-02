import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import net.datafaker.Faker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

public class OrderCreateTest {
    private static final Faker faker = new Faker();
    private static String sharedAccessToken;
    private static User sharedUser;

    private User createUniqueUser() {
        User user = new User();
        user.setEmail(faker.internet().safeEmailAddress());
        user.setName(faker.name().fullName());
        user.setPassword(faker.internet().password(8, 16));
        return user;
    }

    @Before
    public void setUp() {
        sharedUser = createUniqueUser();

        Response createResponse = UserMethods.createUser(sharedUser);

        if (createResponse.getStatusCode() == 403) {
            if (sharedAccessToken != null) {
                UserMethods.deleteUser(sharedAccessToken);
            }
            createResponse = UserMethods.createUser(sharedUser);
            createResponse.then().statusCode(200).body("success", equalTo(true));
        } else {
            createResponse.then().statusCode(200).body("success", equalTo(true));
        }

        Response loginResponse = UserMethods.loginUser(sharedUser);

        loginResponse.then().log().all()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("accessToken", notNullValue());

        String rawToken = loginResponse.then().extract().path("accessToken");

        if (rawToken != null && rawToken.startsWith("Bearer ")) {
            sharedAccessToken = rawToken.substring(7);
        } else {
            sharedAccessToken = rawToken;
        }
    }

    @Test
    @DisplayName("Создание заказа с авторизацией и с ингредиентами")
    @Description("Создание заказа с авторизацией и с ингредиентами - Ожидание 200")
    public void createOrderWithAuthWithIngredients() {
        String localToken = sharedAccessToken;

        List<String> ingredients = Arrays.asList(
                "61c0c5a71d1f82001bdaaa6d",
                "61c0c5a71d1f82001bdaaa70",
                "61c0c5a71d1f82001bdaaa73",
                "61c0c5a71d1f82001bdaaa6d"
        );

        Order order = new Order(ingredients);

        Response orderResponse = OrderActions.createOrderWithAuthorization(order, localToken);

        orderResponse.then().log().all()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("name", notNullValue())
                .body("order.number", notNullValue());
    }

    @Test
    @DisplayName("Создание заказа с авторизацией и без ингредиентов")
    @Description("Создание заказа с авторизацией и без ингредиентами - Ожидание 400")
    public void createOrderWithAuthNoIngredients() {
        String localToken = sharedAccessToken;

        Order order = new Order(new ArrayList<>());

        Response orderResponse = OrderActions.createOrderWithAuthorization(order, localToken);

        orderResponse.then().log().all()
                .statusCode(400)
                .body("success", equalTo(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Создание заказа без авторизации и с ингредиентами")
    @Description("Создание заказа без авторизации и с ингредиентами - Ожидание 401")
    public void createOrderNoAuthWithIngredients() {
        List<String> ingredients = Arrays.asList(
                "61c0c5a71d1f82001bdaaa6d",
                "61c0c5a71d1f82001bdaaa70",
                "61c0c5a71d1f82001bdaaa73",
                "61c0c5a71d1f82001bdaaa6d"
        );
        Order order = new Order(ingredients);

        Response orderResponse = OrderActions.createOrderWithoutAuthorization(order);

        orderResponse.then().log().all()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

    @Test
    @DisplayName("Создание заказа с авторизацией и c невалидным ингредиентом")
    @Description("Создание заказа с авторизацией и c c невалидным ингредиентом - Ожидание 500")
    public void createOrderWithAuthIngredientsNotValid() {
        String localToken = sharedAccessToken;

        List<String> ingredients = Arrays.asList(
                "61c0c5a71d1f82001bdaaa6d",
                "61c0c5a71d1f82001bdaaa70",
                "61c0c5a71d1f82001bdaaa73",
                "daaa6d"
        );
        Order order = new Order(ingredients);

        Response orderResponse = OrderActions.createOrderWithAuthorization(order, localToken);

        orderResponse.then().log().all()
                .statusCode(500);
    }

    @After
    public void tearDown() {
        if (sharedAccessToken != null && !sharedAccessToken.isBlank()) {
                UserMethods.deleteUser(sharedAccessToken);
            }
        }
}
