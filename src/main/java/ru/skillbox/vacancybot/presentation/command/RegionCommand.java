package ru.skillbox.vacancybot.presentation.command;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.skillbox.vacancybot.exception.InvalidInputException;
import ru.skillbox.vacancybot.model.Region;
import ru.skillbox.vacancybot.presentation.dto.UserRequest;
import ru.skillbox.vacancybot.presentation.keyboard.KeyboardFactory;
import ru.skillbox.vacancybot.service.RegionCatalog;
import ru.skillbox.vacancybot.service.UserService;

import java.util.ArrayList;
import java.util.List;

/**
 * Команда /region — выбор региона поиска.
 * <p>
 * Обрабатывает три сценария:
 * <ul>
 *   <li>"/region" или нажатие кнопки "Регион" — показывает первую страницу списка;</li>
 *   <li>"/region 52" или нажатие кнопки региона — сохраняет выбор;</li>
 *   <li>"region:page:20" — листание страниц.</li>
 * </ul>
 */
public class RegionCommand implements BotCommand {

    public static final String NAME = "region";

    /** Префикс аргумента для листания: region:page:20 */
    private static final String PAGE_PREFIX = "page:";

    private final UserService userService;
    private final RegionCatalog regionCatalog;
    private final KeyboardFactory keyboardFactory;

    public RegionCommand(UserService userService,
                         RegionCatalog regionCatalog,
                         KeyboardFactory keyboardFactory) {
        this.userService = userService;
        this.regionCatalog = regionCatalog;
        this.keyboardFactory = keyboardFactory;
    }

    @Override
    public boolean supports(UserRequest request) {
        return NAME.equals(request.command());
    }

    @Override
    public SendMessage execute(UserRequest request) {
        if (!request.hasArgument()) {
            return showPage(request, 0);
        }
        if (request.argument().startsWith(PAGE_PREFIX)) {
            int offset = parseOffset(request.argument().substring(PAGE_PREFIX.length()));
            return showPage(request, offset);
        }
        return selectRegion(request);
    }

    private SendMessage selectRegion(UserRequest request) {
        int code = parseCode(request.argument());

        Region region = regionCatalog.findByCode(code)
                .orElseThrow(() -> new InvalidInputException(
                        "Региона с кодом " + code + " не существует. "
                                + "Выберите его из списка командой /region"));

        userService.setRegion(request.userId(), region.code());

        return SendMessage.builder()
                .chatId(request.chatId().toString())
                .text("Выбран регион: " + region.name() + " [" + region.code() + "]")
                .replyMarkup(keyboardFactory.single(
                        KeyboardFactory.Button.of("Menu", MenuCommand.NAME)))
                .build();
    }

    private SendMessage showPage(UserRequest request, int offset) {
        List<KeyboardFactory.Button> items = new ArrayList<>();
        for (Region region : regionCatalog.getPage(offset)) {
            items.add(KeyboardFactory.Button.of(
                    region.code() + " " + region.name(), NAME, String.valueOf(region.code())));
        }

        List<KeyboardFactory.Button> navigation = new ArrayList<>();
        if (regionCatalog.hasPrevious(offset)) {
            navigation.add(KeyboardFactory.Button.of("Предыдущие 10", NAME,
                    PAGE_PREFIX + (offset - RegionCatalog.PAGE_SIZE)));
        }
        if (regionCatalog.hasNext(offset)) {
            navigation.add(KeyboardFactory.Button.of("Следующие 10", NAME,
                    PAGE_PREFIX + (offset + RegionCatalog.PAGE_SIZE)));
        }

        return SendMessage.builder()
                .chatId(request.chatId().toString())
                .text("Выбирайте регион:")
                .replyMarkup(keyboardFactory.withNavigation(items, navigation))
                .build();
    }

    private int parseCode(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new InvalidInputException(
                    "Код региона должен быть числом. Пример: /region 78");
        }
    }

    private int parseOffset(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
