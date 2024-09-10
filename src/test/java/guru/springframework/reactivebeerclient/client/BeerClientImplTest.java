package guru.springframework.reactivebeerclient.client;

import guru.springframework.reactivebeerclient.config.WebClientConfig;
import guru.springframework.reactivebeerclient.model.BeerDto;
import guru.springframework.reactivebeerclient.model.BeerPagedList;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class BeerClientImplTest {

    BeerClientImpl beerClient;

    @BeforeEach
    void setUp() {
        beerClient = new BeerClientImpl(new WebClientConfig().webClient());
    }

    @Test
    void getBeerById() {
        Mono<BeerDto> beerDtoMono = beerClient.getBeerById(null, null);

        BeerDto beerDto = beerDtoMono.block();

        assertThat(beerDto).isNotNull();
        assertThat(beerDto.getBeerName()).isNotNull();
        log.info("Found Beer: {}", beerDto);
    }

    @Test
    void getBeerByIdFromBerList_BeerExists() {
        // Found here: https://api.springframework.guru/api/v1/beer
        UUID id = UUID.fromString("2bae80c2-0450-4cd0-bed7-eb0ca2fe46b7");

        Mono<BeerDto> beerDtoMono = beerClient
                .listBeers(null, null, null, null, null)
                .flatMap(beerList -> Mono.justOrEmpty(
                        beerList.stream()
                                .filter(beer -> beer.getId().equals(id))
                                .findFirst()
                ));

        BeerDto beerDto = beerDtoMono.block();

        assertThat(beerDto).isNotNull();
        assertThat(beerDto.getBeerName()).isNotNull();
        log.info("Found Beer: {}", beerDto);
    }

    @Test
    void getBeerByIdFromBerList_NoRecords() {
        // Found here: https://api.springframework.guru/api/v1/beer
        UUID id = UUID.randomUUID(); // Anything

        Mono<BeerDto> beerDtoMono = beerClient
                .listBeers(null, null, null, null, null)
                .flatMap(beerList -> Mono.justOrEmpty(
                        beerList.stream()
                                .filter(beer -> beer.getId().equals(id))
                                .findFirst()
                ));

        BeerDto beerDto = beerDtoMono.block();

        // Nothing found as expected
        assertThat(beerDto).isNull();

        log.info("Found Beer: {}", beerDto);
    }

    @Test
    void listBeers() {
        Mono<BeerPagedList> beerPagedListMono =
                beerClient.listBeers(null, null, null, null, null);

        BeerPagedList pagedList = beerPagedListMono.block();

        beerPagedListMono.map(beerList -> beerList.stream()
                        .filter(beer -> beer.getId().equals(1)).findFirst());

        assertThat(pagedList).isNotNull();
        assertThat(pagedList.getContent().size()).isGreaterThan(0);
        log.info("Beer List: {}", pagedList);
    }

    @Test
    void listBeersPageSize() {
        Mono<BeerPagedList> beerPagedListMono =
                beerClient.listBeers(1, 10, null, null, null);

        BeerPagedList pagedList = beerPagedListMono.block();

        assertThat(pagedList).isNotNull();
        assertThat(pagedList.getContent().size()).isEqualTo(10);
        log.info("Beer List: {}", pagedList);
    }

    @Test
    void listBeersNoRecords() {
        Mono<BeerPagedList> beerPagedListMono =
                beerClient.listBeers(10, 20, null, null, null);

        BeerPagedList pagedList = beerPagedListMono.block();

        assertThat(pagedList).isNotNull();
        assertThat(pagedList.getContent().size()).isEqualTo(0);
        log.info("Beer List: {}", pagedList);
    }

    @Test
    void createBeer() {
    }

    @Test
    void updateBeer() {
    }

    @Test
    void deleteBeerById() {
    }

    @Test
    void getBeerByUPCFromBerList_BeerExist() {
        // Found here: https://api.springframework.guru/api/v1/beer
        String upc = "0631234200036";

        Mono<BeerDto> beerDtoMono = beerClient
                .listBeers(null, null, null, null, null)
                .flatMap(beerList -> Mono.justOrEmpty(
                        beerList.stream()
                                .filter(beer -> beer.getUpc().equals(upc))
                                .findFirst()
                ));

        BeerDto beerDto = beerDtoMono.block();

        assertThat(beerDto).isNotNull();
        assertThat(beerDto.getBeerName()).isNotNull();
        log.info("Found Beer: {}", beerDto);
    }

    @Test
    void getBeerByUPCFromBerList_NoRecords() {
        // Found here: https://api.springframework.guru/api/v1/beer
        String upc = "Anything";

        Mono<BeerDto> beerDtoMono = beerClient
                .listBeers(null, null, null, null, null)
                .flatMap(beerList -> Mono.justOrEmpty(
                        beerList.stream()
                                .filter(beer -> beer.getUpc().equals(upc))
                                .findFirst()
                ));

        BeerDto beerDto = beerDtoMono.block();

        assertThat(beerDto).isNull();
        log.info("Found Beer: {}", beerDto);
    }

    @Test
    void getBeerByUPC() {
    }
}