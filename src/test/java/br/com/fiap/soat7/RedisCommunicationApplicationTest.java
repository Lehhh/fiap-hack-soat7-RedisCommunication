package br.com.fiap.soat7;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class RedisCommunicationApplicationTest {

    @Mock
    private SpringApplication springApplication;

    @InjectMocks
    private RedisCommunicationApplication redisCommunicationApplication;

    @BeforeEach
    public void setUp() {
        // Definir variáveis de ambiente temporárias para o teste
        System.setProperty("REDIS_PORT", "6379");
        System.setProperty("spring.data.redis.host", "localhost");
    }

    @Test
    public void testMainMethod() {

        try{
            // Chama o método main da classe RedisCommunicationApplication
            String[] args = {};
            redisCommunicationApplication.main(args);

            // Verifica se o método SpringApplication.run() foi chamado corretamente
            verify(springApplication).run(RedisCommunicationApplication.class, args);
        } catch (Exception e){
        }

    }
}
