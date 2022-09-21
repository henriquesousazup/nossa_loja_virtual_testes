package br.com.zup.edu.nossalojavirtual.categories;

import br.com.zup.edu.nossalojavirtual.util.ExceptionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@ActiveProfiles("test")
class CategoryControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    CategoryRepository categoryRepository;

    private final String apiUrl = "/api/categories";

    @AfterEach
    void tearDown() {
        categoryRepository.deleteAll();
    }

    @Test
    @DisplayName("Should create a new category")
    void test1() throws Exception {

        NewCategoryRequest newCategoryRequest = new NewCategoryRequest("Eletrônicos", null);

        String payload = mapper.writeValueAsString(newCategoryRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(apiUrl)
                .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_lojavirtual:write")))
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("/api/categories/*"));

        assertEquals(1, categoryRepository.findAll().size());
    }

    @Test
    @DisplayName("Should create a new category with a super category")
    void test2() throws Exception {

        Category firstSuperCategory = new Category("Eletrônicos");
        categoryRepository.save(firstSuperCategory);

        Category secondSuperCategory = new Category("Portáteis", firstSuperCategory);
        categoryRepository.save(secondSuperCategory);

        NewCategoryRequest newCategoryRequest = new NewCategoryRequest("Celulares", secondSuperCategory.getId());

        String payload = mapper.writeValueAsString(newCategoryRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(apiUrl)
                .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_lojavirtual:write")))
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON);

        String location = mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("/api/categories/*"))
                .andReturn()
                .getResponse()
                .getHeader("location");

        assertNotNull(location);
        long idNewSavedCategory = Long.parseLong(location.substring(location.lastIndexOf("/") + 1));

        Optional<Category> possibleNewSavedCategory = categoryRepository.findCategoryById(idNewSavedCategory);

        assertTrue(possibleNewSavedCategory.isPresent());
        Category newSavedCategory = possibleNewSavedCategory.get();

        assertEquals(secondSuperCategory.getId(), newSavedCategory.getSuperCategory().getId());
        assertTrue(categoryRepository.existsById(idNewSavedCategory));

        assertEquals(firstSuperCategory.getId(), newSavedCategory.getCategoryHierarchy().get(0).getId());
        assertEquals(secondSuperCategory.getId(), newSavedCategory.getCategoryHierarchy().get(1).getId());
    }

    @Test
    @DisplayName("Should not create a new category with empty name")
    void test3() throws Exception {
        NewCategoryRequest newCategoryRequest = new NewCategoryRequest("", null);

        String payload = mapper.writeValueAsString(newCategoryRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(apiUrl)
                .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_lojavirtual:write")))
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON);

        Exception resolvedException = mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResolvedException();

        MethodArgumentNotValidException exception = (MethodArgumentNotValidException) resolvedException;

        assertNotNull(exception);
        List<String> errorMessages = exception.getBindingResult().getFieldErrors().stream().map(ExceptionUtil::getFieldAndDefaultErrorMessage).toList();

        assertThat(errorMessages, containsInAnyOrder(
                "name must not be empty"
        ));
    }

    @Test
    @DisplayName("Should not create a new category if super category id is invalid")
    void test4() throws Exception {

        NewCategoryRequest newCategoryRequest = new NewCategoryRequest("Eletrônicos", Long.MAX_VALUE);

        String payload = mapper.writeValueAsString(newCategoryRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(apiUrl)
                .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_lojavirtual:write")))
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON);

        Exception resolvedException = mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResolvedException();

        MethodArgumentNotValidException exception = (MethodArgumentNotValidException) resolvedException;

        assertNotNull(exception);
        String errorMessage = exception.getBindingResult().getFieldErrors().get(0).getDefaultMessage();

        assertEquals("The super category does not exists", errorMessage);
    }

    @Test
    @DisplayName("Should not create a new category if name is already registered")
    void test5() throws Exception {

        Category category = new Category("Eletrônicos");
        categoryRepository.save(category);

        NewCategoryRequest newCategoryRequest = new NewCategoryRequest("Eletrônicos", category.getId());

        String payload = mapper.writeValueAsString(newCategoryRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(apiUrl)
                .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_lojavirtual:write")))
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON);

        Exception resolvedException = mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResolvedException();

        MethodArgumentNotValidException exception = (MethodArgumentNotValidException) resolvedException;

        assertNotNull(exception);
        String errorMessage = exception.getBindingResult().getFieldErrors().get(0).getDefaultMessage();

        assertEquals("name is already registered", errorMessage);
    }

    @Test
    @DisplayName("Should not create a new category without token")
    void test6() throws Exception {
        NewCategoryRequest newCategoryRequest = new NewCategoryRequest("Eletrônicos", null);

        String payload = mapper.writeValueAsString(newCategoryRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(apiUrl)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @DisplayName("Should not create a new category without a valid scope")
    void test7() throws Exception {

        NewCategoryRequest newCategoryRequest = new NewCategoryRequest("Eletrônicos", null);

        String payload = mapper.writeValueAsString(newCategoryRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(apiUrl)
                .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_lojavirtual:read")))
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

}