package br.com.zup.edu.nossalojavirtual.categories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;

class CategoryTest {

    @Test
    @DisplayName("Should throw a Illegal Argument Exception in case of name is null")
    void test1() {

        Executable executable = () -> new Category(null);

        IllegalArgumentException illegalArgumentException = assertThrows(
                IllegalArgumentException.class,
                executable
        );

        assertEquals("name must not be empty", illegalArgumentException.getMessage());
    }

    @Test
    @DisplayName("Should throw a Illegal Argument Exception in case of super category is null")
    void test2() {

        Executable executable = () -> new Category("testando", null);

        IllegalArgumentException illegalArgumentException = assertThrows(
                IllegalArgumentException.class,
                executable
        );

        assertEquals("superCategory must not be null using this constructor", illegalArgumentException.getMessage());
    }

}