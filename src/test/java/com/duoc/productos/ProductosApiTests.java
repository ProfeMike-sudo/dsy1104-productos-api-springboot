package com.duoc.productos;

import com.duoc.productos.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

// Las pruebas usan una base H2 en memoria: no modifican los productos de la demostración.
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:pruebas;DB_CLOSE_DELAY=-1",
        "cors.origenes=http://localhost:5173"
})
class ProductosApiTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ProductoRepository repository;

    @BeforeEach
    void limpiar() {
        repository.deleteAll();
    }

    private String json(String nombre, String precio, String stock) {
        return "{\"nombre\":" + nombre + ",\"precio\":" + precio + ",\"stock\":" + stock + "}";
    }

    @Test
    void crudCompletoConDtoDeSalida() throws Exception {
        // El cliente intenta imponer id y fechaCreacion: el DTO de entrada los descarta.
        String cuerpo = "{\"id\":999,\"nombre\":\"  Cuaderno  \",\"precio\":2500,\"stock\":8,\"fechaCreacion\":\"2000-01-01\"}";
        MvcResult creado = mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/productos/")))
                .andExpect(jsonPath("$.nombre").value("Cuaderno"))
                .andExpect(jsonPath("$.precio").value(2500))
                .andExpect(jsonPath("$.stock").value(8))
                .andExpect(jsonPath("$.id").value(not(999)))
                .andExpect(jsonPath("$.fechaCreacion").doesNotExist())
                .andReturn();
        String id = com.jayway.jsonpath.JsonPath.read(creado.getResponse().getContentAsString(), "$.id").toString();

        mvc.perform(get("/api/productos")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/api/productos/" + id)).andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Cuaderno"));
        mvc.perform(put("/api/productos/" + id).contentType(MediaType.APPLICATION_JSON).content(json("\"Libreta\"", "3000", "5")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Libreta"))
                .andExpect(jsonPath("$.stock").value(5));
        mvc.perform(delete("/api/productos/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/productos/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void rechazaEntradasInvalidasSinGuardarlas() throws Exception {
        String[] invalidas = {
                json("\"a\"", "100", "1"),
                json("\"  a  \"", "100", "1"),
                json("\"Cuaderno\"", "-1", "1"),
                json("\"Cuaderno\"", "\"abc\"", "1"),
                json("\"Cuaderno\"", "100", "2.5"),
                json("\"Cuaderno\"", "100", "-3")
        };
        for (String cuerpo : invalidas) {
            mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").isString())
                    .andExpect(jsonPath("$.detalles").isArray());
        }
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content("{mal"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("JSON mal formado o con tipos incorrectos."));
        mvc.perform(get("/api/productos")).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void putExigeCuerpoCompletoYLosRecursosInexistentesDan404() throws Exception {
        mvc.perform(put("/api/productos/12345").contentType(MediaType.APPLICATION_JSON).content(json("\"Cuaderno\"", "100", "1")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Producto no encontrado."));
        mvc.perform(delete("/api/productos/12345")).andExpect(status().isNotFound());
        mvc.perform(get("/api/productos/abc")).andExpect(status().isNotFound());
        mvc.perform(get("/api/otra-ruta")).andExpect(status().isNotFound()).andExpect(jsonPath("$.mensaje").value("Ruta no encontrada."));

        MvcResult creado = mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content(json("\"Cuaderno\"", "100", "1")))
                .andExpect(status().isCreated()).andReturn();
        String id = com.jayway.jsonpath.JsonPath.read(creado.getResponse().getContentAsString(), "$.id").toString();
        mvc.perform(put("/api/productos/" + id).contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"Solo nombre\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Producto inválido."));
    }

    @Test
    void corsPermiteElOrigenConfiguradoYResponderAlPreflight() throws Exception {
        mvc.perform(options("/api/productos")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
        mvc.perform(options("/api/productos")
                        .header("Origin", "http://otro-sitio.com")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/health")).andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("ok"));
    }
}
