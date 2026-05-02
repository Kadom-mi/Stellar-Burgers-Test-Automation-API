import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import static io.restassured.RestAssured.given;

public class OrderActions {

    @Step("Создание заказа с авторизацией")
    public static Response createOrderWithAuthorization(Order order, String accessToken) {
        return given()
                .contentType(ContentType.JSON)
                .baseUri(Api.BASE_URL)
                .header("Authorization", accessToken)
                .body(order)
                .when()
                .post(Api.ORDER_CREATE)
                .then()
                .log().all()
                .extract().response();
    }

    @Step("Создание заказа без авторизации")
    public static Response createOrderWithoutAuthorization(Order order) {
        return given()
                .contentType(ContentType.JSON)
                .baseUri(Api.BASE_URL)
                .body(order)
                .when()
                .post(Api.ORDER_CREATE)
                .then()
                .log().all()
                .extract().response();
    }
}
