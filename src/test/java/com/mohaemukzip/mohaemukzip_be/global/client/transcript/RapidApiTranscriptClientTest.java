package com.mohaemukzip.mohaemukzip_be.global.client.transcript;
import com.mohaemukzip.mohaemukzip_be.global.exception.BusinessException;
import com.mohaemukzip.mohaemukzip_be.global.response.code.status.ErrorStatus;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.*;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;
import java.util.concurrent.TimeoutException;
import static org.assertj.core.api.Assertions.*;
class RapidApiTranscriptClientTest {
    RapidApiTranscriptClient client(int status,String body) {
        WebClient web=WebClient.builder().exchangeFunction(r->Mono.just(ClientResponse.create(HttpStatusCode.valueOf(status))
                .header("Content-Type","application/json").body(body).build())).build();
        return new RapidApiTranscriptClient(web,new SimpleMeterRegistry());
    }
    @ParameterizedTest @CsvSource({"429,SERVICE_UNAVAILABLE","500,SERVICE_UNAVAILABLE","503,SERVICE_UNAVAILABLE","401,EXTERNAL_API_ERROR","403,EXTERNAL_API_ERROR","404,EXTERNAL_API_ERROR","400,EXTERNAL_API_ERROR"})
    void httpErrorsAreNotMissingCaptions(int status,ErrorStatus expected) {
        assertThatThrownBy(()->client(status,"").fetchTranscript("video"))
                .isInstanceOfSatisfying(BusinessException.class,e->assertThat(e.getBaseCode()).isEqualTo(expected));
    }
    @Test void validResponse() {
        assertThat(client(200,"{\"success\":true,\"transcript\":[{\"text\":\"볶아요\",\"offset\":0,\"duration\":1}]}").fetchTranscript("video")).hasSize(1);
    }
    @Test void malformedJsonIsProviderError() {
        assertThatThrownBy(()->client(200,"bad").fetchTranscript("video"))
                .isInstanceOfSatisfying(BusinessException.class,e->assertThat(e.getBaseCode()).isEqualTo(ErrorStatus.EXTERNAL_API_ERROR));
    }
    @Test void confirmedEmptyCaptions() {
        assertThatThrownBy(()->client(200,"{\"success\":true,\"transcript\":[]}").fetchTranscript("video")).isInstanceOf(TranscriptNotFoundException.class);
    }
    @Test void providerFailureIsNotNoCaptions() {
        assertThatThrownBy(()->client(200,"{\"success\":false}").fetchTranscript("video"))
                .isInstanceOfSatisfying(BusinessException.class,e->assertThat(e.getBaseCode()).isEqualTo(ErrorStatus.EXTERNAL_API_ERROR));
    }
    @Test void emptyBodyIsProviderError() {
        assertThatThrownBy(()->client(204,"").fetchTranscript("video"))
                .isInstanceOfSatisfying(BusinessException.class,e->assertThat(e.getBaseCode()).isEqualTo(ErrorStatus.EXTERNAL_API_ERROR));
    }
    @Test void timeoutAndNetworkErrorsHaveSameServiceClassification() {
        for (Throwable cause : new Throwable[]{new TimeoutException(),new IllegalStateException("connection")}) {
            var c=new RapidApiTranscriptClient(WebClient.builder().exchangeFunction(r->Mono.error(cause)).build(),new SimpleMeterRegistry());
            assertThatThrownBy(()->c.fetchTranscript("video"))
                .isInstanceOfSatisfying(BusinessException.class,e->assertThat(e.getBaseCode()).isEqualTo(ErrorStatus.SERVICE_UNAVAILABLE));
        }
    }
    @Test void fallbackPreservesBusinessException() {
        var failure=new TranscriptNotFoundException();
        assertThatThrownBy(()->client(200,"").fallbackFetchTranscript("video",failure)).isSameAs(failure);
    }
    @Test void openCircuitIsUnavailable() {
        assertThatThrownBy(()->client(200,"").fallbackFetchTranscript("video",new IllegalStateException()))
                .isInstanceOfSatisfying(BusinessException.class,e->assertThat(e.getBaseCode()).isEqualTo(ErrorStatus.SERVICE_UNAVAILABLE));
    }
}
