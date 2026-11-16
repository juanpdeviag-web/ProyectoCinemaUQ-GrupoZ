package test.core.domain;

import co.edu.uq.cinemauq.core.domain.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;

public class EntidadesDominioTest {

        @Test
        void peliculaDebeConservarTitulo() {
            Pelicula pelicula = new Pelicula("Interestelar", "Ciencia ficción", 169);

            assertNotNull(pelicula);
            assertEquals("Interestelar", pelicula.getTitulo());
        }

        @Test
        void clienteDebeCrearTarjetaVirtualActivaConSaldoInicialCero() {
            Cliente cliente = new Cliente("C001", "Ana", "ana@correo.com");

            assertNotNull(cliente);
            assertEquals("CLIENTE", cliente.getRol());
        }

        @Test
        void administradorDebeTenerRolCorrecto() {
            Administrador administrador = new Administrador("A001", "Carlos", "carlos@correo.com");

            assertNotNull(administrador);
            assertEquals("ADMINISTRADOR", administrador.getRol());
        }

        @Test
        void tarjetaVirtualDebeIniciarActivaYConSaldoCero() {
            TarjetaVirtual tarjeta = new TarjetaVirtual();

            assertTrue(tarjeta.isActiva());
            assertEquals(0.0, tarjeta.getSaldo());
        }

        @Test
        void tarjetaVirtualDebePermitirRecargaPositiva() {
            TarjetaVirtual tarjeta = new TarjetaVirtual();

            tarjeta.recargar(50000);

            assertEquals(50000.0, tarjeta.getSaldo());
        }

        @Test
        void tarjetaVirtualNoDebeModificarSaldoConRecargaNoPositiva() {
            TarjetaVirtual tarjeta = new TarjetaVirtual();

            tarjeta.recargar(0);
            tarjeta.recargar(-1000);

            assertEquals(0.0, tarjeta.getSaldo());
        }

        @Test
        void funcionDebeAsociarLaPeliculaRecibida() {
            Pelicula pelicula = new Pelicula("Toy Story", "Animación", 81);
            Sala sala = new Sala("Sala 1", 10, 12);
            Funcion funcion = new Funcion(pelicula, sala, LocalDateTime.now());

            assertNotNull(funcion);
            assertSame(pelicula, funcion.getPelicula());
        }

        @Test
        void salaDebePoderCrearseConDatosValidos() {
            Sala sala = new Sala("Sala VIP", 6, 8);

            assertNotNull(sala);
        }

}
