package ru.skillbox.vacancybot.presentation.formatter;

import ru.skillbox.vacancybot.model.Vacancy;

import java.util.List;

public class VacancyFormatter {

    public static final int PAGE_SIZE = 10;

    public String formatPage(List<Vacancy> vacancies, int offset) {
        if (vacancies.isEmpty()) {
            return "По вашим критериям новых вакансий не найдено.";
        }

        int from = Math.max(0, Math.min(offset, vacancies.size()));
        int to = Math.min(from + PAGE_SIZE, vacancies.size());

        StringBuilder message = new StringBuilder();
        for (Vacancy vacancy : vacancies.subList(from, to)) {
            message.append(format(vacancy)).append("\n\n");
        }
        message.append("Показаны вакансии ")
                .append(from + 1).append("-").append(to)
                .append(" из ").append(vacancies.size());

        return message.toString();
    }

    public String format(Vacancy vacancy) {
        StringBuilder text = new StringBuilder()
                .append("Компания: \"").append(escape(vacancy.getCompanyName())).append("\"\n");

        if (vacancy.getJobName() != null) {
            text.append("Должность: ").append(escape(vacancy.getJobName())).append("\n");
        }

        text.append("Зарплата: ").append(formatSalary(vacancy)).append("\n")
                .append("Опыт: ").append(formatExperience(vacancy)).append("\n");

        if (vacancy.getUrl() != null) {
            text.append("Подробное описание вакансии <a href=\"")
                    .append(vacancy.getUrl())
                    .append("\">здесь</a>");
        }

        return text.toString();
    }

    private String formatSalary(Vacancy vacancy) {
        Integer min = vacancy.getSalaryMin();
        Integer max = vacancy.getSalaryMax();

        if (min == null && max == null) {
            return "по договорённости";
        }
        if (min != null && max != null) {
            return min + "-" + max + " руб.";
        }
        return (min != null ? "от " + min : "до " + max) + " руб.";
    }

    private String formatExperience(Vacancy vacancy) {
        return vacancy.getExperience() == null ? "не указан" : vacancy.getExperience() + ".";
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}