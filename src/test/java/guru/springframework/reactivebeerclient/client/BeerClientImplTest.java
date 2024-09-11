package guru.springframework.reactivebeerclient.client;

import guru.springframework.reactivebeerclient.config.WebClientConfig;
import guru.springframework.reactivebeerclient.model.BeerDto;
import guru.springframework.reactivebeerclient.model.BeerPagedList;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
class BeerClientImplTest {

    BeerClientImpl beerClient;

    // By default, empty
    private BeerDto beerDto = BeerDto.builder().build();

    @BeforeEach
    void setUp() {
        beerClient = new BeerClientImpl(new WebClientConfig().webClient());

        Mono<BeerPagedList> beerPagedListMono =
                beerClient.listBeers(null, null, null, null, null);

        BeerPagedList pagedList = beerPagedListMono.block();

        beerDto = pagedList.getContent().get(0);
    }

    @Test
    void getBeerById() {
        // Found here: https://api.springframework.guru/api/v1/beer
        Mono<BeerDto> beerDtoMono = beerClient.getBeerById(beerDto.getId(), false);

        BeerDto beerDto = beerDtoMono.block();

        assertThat(beerDto).isNotNull();
        assertThat(beerDto.getBeerName()).isNotNull();
        log.info("Found Beer: {}", beerDto);
    }

    @Test
    void getBeerByIdFromBeerList() {
        Mono<BeerDto> beerDtoMono = beerClient.getBeerById(beerDto.getId(), false);

        BeerDto beerDto = beerDtoMono.block();

        assertThat(beerDto.getId()).isEqualTo(beerDto.getId());
        // assertThat(beerDto.getQuantityOnHand()).isNull();
        log.info("Found Beer: {}", beerDto);
    }

    @Test
    void getBeerByIdFromBeerList_ShowInventoryTrue() {
        Mono<BeerDto> beerDtoMono = beerClient.getBeerById(beerDto.getId(), true);

        BeerDto beerDto = beerDtoMono.block();

        assertThat(beerDto.getId()).isEqualTo(beerDto.getId());
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
                                .filter(beer -> beer.getId().equals(beerDto.getId()))
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
        BeerDto updatedBeerDto = BeerDto.builder()
                .beerName("Really Good Bear")
                .beerStyle(beerDto.getBeerStyle())
                .price(beerDto.getPrice())
                .upc(beerDto.getUpc())
                .build();

        Mono<ResponseEntity<Void>> responseEntityMono = beerClient.updateBeer(beerDto.getId(), updatedBeerDto);
        ResponseEntity responseEntity = responseEntityMono.block();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void testDeleteBeerHandleException() {
        Mono<ResponseEntity<Void>> responseEntityMono = beerClient.deleteBeerById(UUID.randomUUID());

        ResponseEntity responseEntity = responseEntityMono.onErrorResume(throwable -> {
            if(throwable instanceof WebClientResponseException) {
                WebClientResponseException exception = (WebClientResponseException) throwable;
                return Mono.just(ResponseEntity.status(exception.getStatusCode()).build());
            } else {
                throw new RuntimeException(throwable);
            }
        }).block();

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Test
    void deleteBeerById_NotFound() {
        Mono<ResponseEntity<Void>> responseEntityMono = beerClient.deleteBeerById(UUID.randomUUID());

        assertThrows(WebClientResponseException.class, () -> {
            ResponseEntity responseEntity = responseEntityMono.block();
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        });
    }

    @Test
    @Disabled
    void deleteBeerById() {
        Mono<ResponseEntity<Void>> responseEntityMono = beerClient.deleteBeerById(beerDto.getId());
        ResponseEntity responseEntity = responseEntityMono.block();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void functionalTestBetBeerById() throws InterruptedException {
        AtomicReference<String> beerName = new AtomicReference<>();
        CountDownLatch countDownLatch = new CountDownLatch(1);

        beerClient.listBeers(null, null, null, null, null)
                .map(beerPagedList -> beerPagedList.getContent().get(0).getId())
                .map(beerId -> beerClient.getBeerById(beerId, false))
                .flatMap(mono -> mono)
                .subscribe(beerDtoSubs -> {
                   log.info("Beer Name: {}", beerDtoSubs.getBeerName());
                   beerName.set(beerDtoSubs.getBeerName());
                   // Not triggered
                   assertThat(beerDtoSubs.getBeerName()).isEqualTo("Mango Bobs");
                   countDownLatch.countDown();
                });

        countDownLatch.await();
        assertThat(beerName.get()).isEqualTo("Mango Bobs");
    }
}