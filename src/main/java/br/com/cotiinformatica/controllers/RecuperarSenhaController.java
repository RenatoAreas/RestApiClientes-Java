package br.com.cotiinformatica.controllers;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import br.com.cotiinformatica.dtos.RecuperarSenhaPostDTO;
import br.com.cotiinformatica.entities.Usuario;
import br.com.cotiinformatica.helpers.PasswordHelper;
import br.com.cotiinformatica.interfaces.IUsuarioRepository;
import br.com.cotiinformatica.security.Criptografia;
import br.com.cotiinformatica.senders.MailSender;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

@Controller
@Transactional
public class RecuperarSenhaController {

	private static final String ENDPOINT = "/api/recuperarsenha";

	@Autowired
	private IUsuarioRepository usuarioRepository;

	@Autowired
	private MailSender mailSender;

	@CrossOrigin
	@RequestMapping(value = ENDPOINT, method = RequestMethod.POST)
	@RequestBody
	public ResponseEntity<String> post(@RequestBody RecuperarSenhaPostDTO dto) {

		Usuario usuario = usuarioRepository.findByEmail(dto.getEmail());

		try {

			if (usuario != null) {

				String novaSenha = PasswordHelper.generateRandomPassword();

				usuario.setSenha(Criptografia.criptografar(novaSenha));
				usuarioRepository.save(usuario);

				enviarNovaSenha(usuario, novaSenha);

				return ResponseEntity.status(HttpStatus.OK) // HTTP 200
						.body("Uma nova senha foi gerada com sucesso. Verifique o seu email !");

			} else {

				return ResponseEntity.status(HttpStatus.BAD_REQUEST) // HTTP 400
						.body("O email já está cadastrado na base de dados !");

			}

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) // HTTP 500
					.body("Erro: " + e.getMessage());

		}
	}

	private void enviarNovaSenha(Usuario usuario, String novaSenha) throws Exception {

		String assunto = "Nova senha gerada com sucesso !";
		String mensagem = "Olá,  " + usuario.getNome() + "\n\n"
				+ "Sua nova senha foi gerada com sucesso, para acessar o sistema utilize a senha" + novaSenha + "\n\n"
				+ "Att, \n" + "Equipe de suporte ";

		mailSender.sendMessage(usuario.getEmail(), assunto, mensagem);
	}

}
