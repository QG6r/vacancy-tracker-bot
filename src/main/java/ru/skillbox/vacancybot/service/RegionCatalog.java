package ru.skillbox.vacancybot.service;

import ru.skillbox.vacancybot.model.Region;

import java.util.List;
import java.util.Optional;

/**
 * Справочник регионов России.
 * <p>
 * Список фиксированный, поэтому хранится прямо в коде — заводить
 * под него репозиторий и файл было бы лишним усложнением.
 */
public class RegionCatalog {

    /** Сколько регионов показывается на одной странице. */
    public static final int PAGE_SIZE = 10;

    private static final List<Region> REGIONS = List.of(
            new Region(1, "Республика Адыгея (Адыгея)"),
            new Region(2, "Республика Башкортостан"),
            new Region(3, "Республика Бурятия"),
            new Region(4, "Республика Алтай"),
            new Region(5, "Республика Дагестан"),
            new Region(6, "Республика Ингушетия"),
            new Region(7, "Кабардино-Балкарская Республика"),
            new Region(8, "Республика Калмыкия"),
            new Region(9, "Карачаево-Черкесская Республика"),
            new Region(10, "Республика Карелия"),
            new Region(11, "Республика Коми"),
            new Region(12, "Республика Марий Эл"),
            new Region(13, "Республика Мордовия"),
            new Region(14, "Республика Саха (Якутия)"),
            new Region(15, "Республика Северная Осетия - Алания"),
            new Region(16, "Республика Татарстан (Татарстан)"),
            new Region(17, "Республика Тыва"),
            new Region(18, "Удмуртская Республика"),
            new Region(19, "Республика Хакасия"),
            new Region(20, "Чеченская Республика"),
            new Region(21, "Чувашская Республика"),
            new Region(22, "Алтайский край"),
            new Region(23, "Краснодарский край"),
            new Region(24, "Красноярский край"),
            new Region(25, "Приморский край"),
            new Region(26, "Ставропольский край"),
            new Region(27, "Хабаровский край"),
            new Region(28, "Амурская область"),
            new Region(29, "Архангельская область"),
            new Region(30, "Астраханская область"),
            new Region(31, "Белгородская область"),
            new Region(32, "Брянская область"),
            new Region(33, "Владимирская область"),
            new Region(34, "Волгоградская область"),
            new Region(35, "Вологодская область"),
            new Region(36, "Воронежская область"),
            new Region(37, "Ивановская область"),
            new Region(38, "Иркутская область"),
            new Region(39, "Калининградская область"),
            new Region(40, "Калужская область"),
            new Region(41, "Камчатский край"),
            new Region(42, "Кемеровская область"),
            new Region(43, "Кировская область"),
            new Region(44, "Костромская область"),
            new Region(45, "Курганская область"),
            new Region(46, "Курская область"),
            new Region(47, "Ленинградская область"),
            new Region(48, "Липецкая область"),
            new Region(49, "Магаданская область"),
            new Region(50, "Московская область"),
            new Region(51, "Мурманская область"),
            new Region(52, "Нижегородская область"),
            new Region(53, "Новгородская область"),
            new Region(54, "Новосибирская область"),
            new Region(55, "Омская область"),
            new Region(56, "Оренбургская область"),
            new Region(57, "Орловская область"),
            new Region(58, "Пензенская область"),
            new Region(59, "Пермский край"),
            new Region(60, "Псковская область"),
            new Region(61, "Ростовская область"),
            new Region(62, "Рязанская область"),
            new Region(63, "Самарская область"),
            new Region(64, "Саратовская область"),
            new Region(65, "Сахалинская область"),
            new Region(66, "Свердловская область"),
            new Region(67, "Смоленская область"),
            new Region(68, "Тамбовская область"),
            new Region(69, "Тверская область"),
            new Region(70, "Томская область"),
            new Region(71, "Тульская область"),
            new Region(72, "Тюменская область"),
            new Region(73, "Ульяновская область"),
            new Region(74, "Челябинская область"),
            new Region(75, "Забайкальский край"),
            new Region(76, "Ярославская область"),
            new Region(77, "Москва"),
            new Region(78, "Санкт-Петербург"),
            new Region(79, "Еврейская автономная область"),
            new Region(83, "Ненецкий автономный округ"),
            new Region(86, "Ханты-Мансийский автономный округ - Югра"),
            new Region(87, "Чукотский автономный округ"),
            new Region(89, "Ямало-Ненецкий автономный округ"),
            new Region(91, "Республика Крым"),
            new Region(92, "Севастополь")
    );

    public List<Region> findAll() {
        return REGIONS;
    }

    public Optional<Region> findByCode(int code) {
        return REGIONS.stream()
                .filter(region -> region.code() == code)
                .findFirst();
    }

    public boolean exists(int code) {
        return findByCode(code).isPresent();
    }

    /**
     * Возвращает страницу регионов.
     *
     * @param offset сколько регионов пропустить от начала списка
     */
    public List<Region> getPage(int offset) {
        int from = Math.max(0, Math.min(offset, REGIONS.size()));
        int to = Math.min(from + PAGE_SIZE, REGIONS.size());
        return REGIONS.subList(from, to);
    }

    public boolean hasPrevious(int offset) {
        return offset > 0;
    }

    public boolean hasNext(int offset) {
        return offset + PAGE_SIZE < REGIONS.size();
    }

    public int size() {
        return REGIONS.size();
    }
}
