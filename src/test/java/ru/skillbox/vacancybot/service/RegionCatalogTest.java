package ru.skillbox.vacancybot.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Справочник регионов")
class RegionCatalogTest {

    private final RegionCatalog catalog = new RegionCatalog();

    @Test
    @DisplayName("находит регион по коду")
    void findsByCode() {
        assertThat(catalog.findByCode(78))
                .isPresent()
                .get()
                .satisfies(region -> assertThat(region.name()).isEqualTo("Санкт-Петербург"));
    }

    @Test
    @DisplayName("не находит несуществующий код")
    void doesNotFindUnknownCode() {
        assertThat(catalog.findByCode(999)).isEmpty();
        assertThat(catalog.exists(999)).isFalse();
    }

    @Test
    @DisplayName("страница содержит 10 регионов")
    void returnsPageOfTen() {
        assertThat(catalog.getPage(0)).hasSize(RegionCatalog.PAGE_SIZE);
    }

    @Test
    @DisplayName("на первой странице нет кнопки назад")
    void noPreviousOnFirstPage() {
        assertThat(catalog.hasPrevious(0)).isFalse();
        assertThat(catalog.hasNext(0)).isTrue();
    }

    @Test
    @DisplayName("на последней странице нет кнопки вперёд")
    void noNextOnLastPage() {
        int lastOffset = (catalog.size() / RegionCatalog.PAGE_SIZE) * RegionCatalog.PAGE_SIZE;

        assertThat(catalog.hasNext(lastOffset)).isFalse();
        assertThat(catalog.hasPrevious(lastOffset)).isTrue();
    }

    @Test
    @DisplayName("смещение за пределами списка не ломает выдачу")
    void handlesOffsetBeyondEnd() {
        assertThat(catalog.getPage(10_000)).isEmpty();
    }
}
