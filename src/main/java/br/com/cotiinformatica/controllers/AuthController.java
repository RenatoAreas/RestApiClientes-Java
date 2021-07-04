package br.com.cotiinformatica.controllers;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import br.com.cotiinformatica.dtos.AuthPostDTO;
import br.com.cotiinformatica.dtos.UsuarioGetDTO;
import br.com.cotiinformatica.entities.Usuario;
import br.com.cotiinformatica.interfaces.IUsuarioRepository;
import br.com.cotiinformatica.security.Criptografia;
import br.com.cotiinformatica.security.TokenSecurity;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

@Controller
@Transactional
public class AuthController {

	private static final String ENDPOINT = "/api/auth";

	@Autowired
	private IUsuarioRepository usuarioRepository;

	@CrossOrigin
	@RequestMapping(value = ENDPOINT, method = RequestMethod.POST)
	@RequestBody
	public ResponseEntity<UsuarioGetDTO> post(@RequestBody AuthPostDTO dto) {

		try {

			// procurando o usuário no banco de dados atraves do email e senha

			Usuario usuario = usuarioRepository.findByEmailAndSenha(dto.getEmail(),
					Criptografia.criptografar(dto.getSenha()));

			if (usuario != null) {

				UsuarioGetDTO result = new UsuarioGetDTO();

				result.setIdUsuario(usuario.getIdusuario());
				result.setNome(usuario.getNome());
				result.setEmail(usuario.getEmail());
				result.setAccessToken(TokenSecurity.generateToken(usuario.getEmail()));

				return ResponseEntity.status(HttpStatus.OK).body(result);
			} else {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
			}

		} catch (Exception e) {

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}

	}

}
