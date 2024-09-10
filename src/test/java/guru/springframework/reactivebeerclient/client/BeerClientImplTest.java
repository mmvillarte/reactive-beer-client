package guru.springframework.reactivebeerclient.client;

import guru.springframework.reactivebeerclient.config.WebClientConfig;
import guru.springframework.reactivebeerclient.model.BeerDto;
import guru.springframework.reactivebeerclient.model.BeerPagedList;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class BeerClientImplTest {

    BeerClientImpl beerClient;

    private UUID beerId;

    @BeforeEach
    void setUp() {
        beerClient = new BeerClientImpl(new WebClientConfig().webClient());

        Mono<BeerPagedList> beerPagedListMono =
                beerClient.listBeers(null, null, null, null, null);

        BeerPagedList pagedList = beerPagedListMono.block();

        beerId = pagedList.getContent().get(0).getId();
    }

    @Test
    void getBeerById() {
        // Found here: https://api.springframework.guru/api/v1/beer
        Mono<BeerDto> beerDtoMono = beerClient.getBeerById(beerId, false);

        BeerDto beerDto = beerDtoMono.block();

        assertThat(beerDto).isNotNull();
        assertThat(beerDto.getBeerName()).isNotNull();
        log.info("Found Beer: {}", beerDto);
    }

    @Test
    void getBeerByIdFromBeerList() {
        Mono<BeerDto> beerDtoMono = beerClient.getBeerById(beerId, false);

        BeerDto beerDto = beerDtoMono.block();

        assertThat(beerDto.getId()).isEqualTo(beerId);
        // assertThat(beerDto.getQuantityOnHand()).isNull();
        log.info("Found Beer: {}", beerDto);
    }

    @Test
    void getBeerByIdFromBeerList_ShowInventoryTrue() {
        Mono<BeerDto> beerDtoMono = beerClient.getBeerById(beerId, true);

        BeerDto beerDto = beerDtoMono.block();

        assertThat(beerDto.getId()).isEqualTo(beerId);
        assertThat(beerDto.getQuantityOnHand()).isNotNull();
        log.info("Found Beer: {}", beerDto);
    }

    @Test
    void getBeerByIdFromBerList_BeerExists() {
        // Found here: https://api.springframework.guru/api/v1/beer

        Mono<BeerDto> beerDtoMono = beerClient
                .listBeers(null, null, null, null, null)
                .flatMap(beerList -> Mono.justOrEmpty(
                        beerList.stream()
                                .filter(beer -> beer.getId().equals(beerId))
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
    void getBeerByUpcFromBeerList() {
        Mono<BeerPagedList> beerPagedListMono =
                beerClient.listBeers(null, null, null, null, null);

        BeerPagedList pagedList = beerPagedListMono.block();

        String beerUpc = pagedList.getContent().get(0).getUpc();

        Mono<BeerDto> beerDtoMono = beerClient.getBeerByUPC(beerUpc);

        BeerDto beerDto = beerDtoMono.block();

        assertThat(beerDto.getUpc()).isEqualTo(beerUpc);
        log.info("Found Beer: {}", beerDto);
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
    void createBeer() {
        BeerDto beerDto = BeerDto.builder()
                .beerName("Dogfihhead 98 Min IPA")
                .beerStyle("IPA")
                .upc("234848549559")
                .price(new BigDecimal("18.99"))
                .build();

        Mono<ResponseEntity<Void>> responseEntityMono = beerClient.createBeer(beerDto);
        ResponseEntity responseEntity = responseEntityMono.block();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void updateBeer() {
    }

    @Test
    void deleteBeerById() {
    }
}