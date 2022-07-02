package org.cezszym.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.cezszym.entity.Book;

@Getter
@AllArgsConstructor
public class BookDTO {

    private Integer id;

    private String title;

    private int author_id;

    private int category_id;

    private String description;

    private int price;

    private int year;

}
