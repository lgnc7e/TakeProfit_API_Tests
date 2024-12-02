package botsystest.tests.bots;

import botsystem.utils.DataProviders;
import botsystest.tests.core.TestBase;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class BotsTestsCSV extends TestBase {
    Logger logger = LoggerFactory.getLogger(BotsTestsCSV.class);
    String idConnectedExch;

    @BeforeMethod
    public void preConditions() {
        idConnectedExch = app.createExchange(false);

    }


    @Test(dataProvider = "botData3", dataProviderClass = DataProviders.class)
    public void botCreateSuccessfulShortPositiveTestCSV(String tradingPair, String type, float deposit, boolean stopLoss, boolean takeProfit, boolean pumpDump, String indicator, int period, String interval) {
        String exchangeId = idConnectedExch;
        String botId = null;

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("exchangeId", exchangeId);
            requestBody.put("tradingPair", tradingPair);
            requestBody.put("type", type);
            requestBody.put("deposit", deposit);
            requestBody.put("stopLoss", stopLoss);
            requestBody.put("takeProfit", takeProfit);
            requestBody.put("pumpDump", pumpDump);

            Map<String, Object> indicatorMap = new HashMap<>();
            indicatorMap.put("indicator", indicator);
            indicatorMap.put("period", period);
            indicatorMap.put("interval", interval);
            requestBody.put("indicators", Collections.singletonList(indicatorMap));

            Response response = given()
                    .header("Authorization", "Bearer " + app.TOKEN)
                    .contentType(ContentType.JSON)
                    .body(requestBody)
                    .when()
                    .post("/bots")
                    .then()
                    .statusCode(201)
                    .body("id", notNullValue())
                    .extract()
                    .response();

            botId = response.jsonPath().getString("id");
            logger.info("Bot created successfully: " + botId);
            logger.info(response.asString());
            app.deleteOneBot(botId);
            logger.info("Deleted bot immediately: " + botId);
        } catch (Exception e) {
            logger.error("Error during bot creation or deletion: " + e.getMessage());
        }

    }

    @Test(dataProvider = "botData2", dataProviderClass = DataProviders.class)
    public void botCreateSuccessfulPositiveTestCSV(String tradingPair, String type, float deposit, boolean stopLoss, boolean takeProfit, boolean pumpDump, String indicator, int period, String interval) {
        String exchangeId = app.getIdDemoExchange();
        String botId = null;
        Response response = null;
        try {

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("exchangeId", exchangeId);
            requestBody.put("tradingPair", tradingPair);
            requestBody.put("type", type);
            requestBody.put("deposit", deposit);
            requestBody.put("stopLoss", stopLoss);
            requestBody.put("takeProfit", takeProfit);
            requestBody.put("pumpDump", pumpDump);

            Map<String, Object> indicatorMap = new HashMap<>();
            indicatorMap.put("indicator", indicator);
            indicatorMap.put("period", period);
            indicatorMap.put("interval", interval);
            requestBody.put("indicators", Collections.singletonList(indicatorMap));

            response = given()
                    .header("Authorization", "Bearer " + app.TOKEN)
                    .contentType(ContentType.JSON)
                    .body(requestBody)
                    .when()
                    .post("/bots")
                    .then()
                    .statusCode(201)
                    .body("id", notNullValue())
                    .body("exchangeId", equalTo(exchangeId))
                    .body("tradingPair", equalTo(tradingPair))
                    .body("type", equalTo(type))
                    .body("deposit", equalTo(deposit))
                    .body("stopLoss", equalTo(stopLoss))
                    .body("takeProfit", equalTo(takeProfit))
                    .body("pumpDump", equalTo(pumpDump))
                    .body("status", equalTo("stop"))
                    .body("indicators[0].indicator", equalTo(indicator))
                    .body("indicators[0].period", equalTo(period))
                    .body("indicators[0].interval", equalTo(interval))
                    .extract()
                    .response();
            botId = response.jsonPath().getString("id");
            logger.info("Bot created successfully: " + botId);
            logger.info(response.asString());
            app.deleteOneBot(botId);
            logger.info("Deleted bot immediately: " + botId);
        } catch (Exception e) {
            logger.error("Error during bot creation or deletion: " + e.getMessage());
        }

        logger.info(response.asString());
    }



    @Test(dataProvider = "tradingPairs", dataProviderClass = DataProviders.class)
    public void botCreateUnsuccesfullNegativeTestCSV(String tradingPair) {
        String exchangeId = app.getIdConnectedExchange()[0];
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("exchangeId", exchangeId);
        requestBody.put("tradingPair", tradingPair);
        requestBody.put("type", "Short");
        requestBody.put("deposit", 0.08);
        requestBody.put("stopLoss", false);
        requestBody.put("takeProfit", false);
        requestBody.put("pumpDump", false);

        Map<String, Object> indicatorMap = new HashMap<>();
        indicatorMap.put("indicator", "STOCHASTIC");
        indicatorMap.put("period", 7);
        indicatorMap.put("interval", "1m");
        requestBody.put("indicators", Collections.singletonList(indicatorMap));


        Response response = given()
                .header("Authorization", "Bearer " + app.TOKEN)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/bots")
                .then()
                .statusCode(400)
                .body("error", equalTo("ExchangeBadRequest"))
                .body("message", equalTo("Trading pair "+ tradingPair + " is not available for the specified exchange."))
                .extract()
                .response();
        logger.info(response.asString());
    }

    @AfterMethod
    public void postCondition() {
        try {
            if (idConnectedExch != null) {
                app.deleteExchange(idConnectedExch);
                logger.info("Exchange with ID " + idConnectedExch + " was deleted.");
            }
        } catch (Exception e) {
            logger.error("Error deleting exchange: " + e.getMessage());
        }
    }

}
