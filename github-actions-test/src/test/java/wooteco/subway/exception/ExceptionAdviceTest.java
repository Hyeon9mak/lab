package wooteco.subway.exception;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import wooteco.subway.AcceptanceTest;
import wooteco.subway.station.dao.StationDao;
import wooteco.subway.station.dto.StationRequest;

class ExceptionAdviceTest extends AcceptanceTest {

    @Autowired
    private StationDao stationDao;
    
    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        stationDao = stationDao.dropTable();
    }

    @DisplayName("MethodArgumentNotValidException 출력문 확인")
    @Test
    void methodArgumentNotValidException() {
        // given
        StationRequest request = new StationRequest(null);

        // when
        ExtractableResponse<Response> response = RestAssured
            .given().log().all()
            .body(request)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when().post("/api/stations")
            .then().log().all()
            .extract();

        List<String> responses = response.jsonPath()
            .getList("message", String.class);

        // then
        assertThat(responses).containsExactly("INVALID_INPUT");
    }

    @DisplayName("ConstraintViolationException 출력문 확인")
    @Test
    void constraintViolationExceptionException() {
        // given
        StationRequest request = new StationRequest(null);

        // when
        ExtractableResponse<Response> response = RestAssured
            .given().log().all()
            .body(request)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when().post("/api/stations")
            .then().log().all()
            .extract();

        List<String> responses = response.jsonPath()
            .getList("message", String.class);

        // then
        assertThat(responses).containsExactly("INVALID_NAME");
    }
}