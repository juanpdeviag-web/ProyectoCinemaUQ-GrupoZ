package co.edu.uq.cinemauq.services;

import co.edu.uq.cinemauq.core.domain.Cliente;
import co.edu.uq.cinemauq.core.domain.TarjetaVirtual;
import co.edu.uq.cinemauq.core.domain.Usuario;
import co.edu.uq.cinemauq.core.ports.ICineService;
import co.edu.uq.cinemauq.core.ports.IPagoProcessor;
import co.edu.uq.cinemauq.core.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CineServiceImpl implements ICineService {
    private static CineServiceImpl instancia;
    private UserRepository repository;
    private IPagoProcessor pagoProcessor;
    private TarjetaVirtualService tarjetaVirtualService;

    private List<Usuario> usuarios;

    private CineServiceImpl() {
        this.usuarios = new ArrayList<>();
    }

    public static synchronized CineServiceImpl getInstancia() {
        if (instancia == null) {
            instancia = new CineServiceImpl();
        }
        return instancia;
    }

    public CineServiceImpl(UserRepository repository) {
        this.repository = Objects.requireNonNull(repository);
        this.pagoProcessor = new PagoProcessorImpl();
        this.tarjetaVirtualService = new TarjetaVirtualService(repository, pagoProcessor);
    }

    public CineServiceImpl(UserRepository repository, IPagoProcessor pagoProcessor) {
        this.repository = Objects.requireNonNull(repository);
        this.pagoProcessor = Objects.requireNonNull(pagoProcessor);
        this.tarjetaVirtualService = new TarjetaVirtualService(repository, pagoProcessor);
    }

    @Override
    public void registrarUsuario(Usuario usuario) {
        repository.guardar(usuario);
    }

    @Override
    public List<Usuario> listUsuarios() {
        return repository.listar();
    }

    @Override
    public List<Cliente> listClientes() {
        return repository.listar().stream()
                .filter(u -> u instanceof Cliente)
                .map(u -> (Cliente) u)
                .toList();
    }

    @Override
    public boolean realizarCompra(String idCliente, double monto) {
        Cliente cliente = buscarCliente(idCliente);
        TarjetaVirtual tarjeta = cliente.getTarjetaVirtual();
        return pagoProcessor.procesarPago(tarjeta, monto);
    }

    @Override
    public void reembolsarCompra(String idCliente, double monto) {
        Cliente cliente = buscarCliente(idCliente);
        TarjetaVirtual tarjeta = cliente.getTarjetaVirtual();
        pagoProcessor.procesarReembolso(tarjeta, monto);
    }

    public TarjetaVirtualService getTarjetaVirtualService() {
        return tarjetaVirtualService;
    }

    private Cliente buscarCliente(String idCliente) {
        return repository.listar().stream()
                .filter(u -> u instanceof Cliente)
                .map(u -> (Cliente) u)
                .filter(c -> c.getId().equals(idCliente))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + idCliente));
    }
}