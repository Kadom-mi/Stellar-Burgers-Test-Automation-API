import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import static io.restassured.RestAssured.given;

public class UserMethods {

    @Step("Создание уникального пользователя")
    public static Response createUser(User user) {
        return given()
                .contentType(ContentType.JSON)
                .baseUri(Api.BASE_URL)
                .body(user)
                .when()
                .post(Api.USER_CREATE)
                .then()
                .log().all()
                .extract().response();
    }

    @Step("Авторизация пользователя")
    public static Response loginUser(User user) {
        return given()
                .contentType(ContentType.JSON)
                .baseUri(Api.BASE_URL)
                .body(user)
                .when()
                .post(Api.USER_AUTH)
                .then()
                .log().all()
                .extract().response();
    }

    @Step("Удаление пользователя")
    public static void deleteUser(String accessToken) {
        given()
                .header("Authorization", accessToken)
                .baseUri(Api.BASE_URL)
                .when()
                .delete(Api.USER_DELETE)
                .then()
                .log().all()
                .extract().response();
    }
}
