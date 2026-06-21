package blog.code.codeblog.service;

import blog.code.codeblog.dto.authentication.AuthenticationDTO;
import blog.code.codeblog.dto.authentication.RegisterRespondeDTO;
import blog.code.codeblog.dto.user.CreateUserDTO;
import blog.code.codeblog.enums.UserRoles;
import blog.code.codeblog.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthenticationServiceTest {

	@InjectMocks
	private AuthenticationService authenticationService;

	@Mock
	private UserService userService;

	@Mock
	private TokenService tokenService;

	@Mock
	private AuthenticationManager authenticationManager;

	@BeforeEach
	@DisplayName("Initial mock setup for AuthenticationServiceTest")
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@DisplayName("Should register a user successfully")
	void registerSuccessfully() {
		CreateUserDTO createUserDTO = new CreateUserDTO(
				"Guilherme", "email@test.com", "123456", UserRoles.COSTUMER, null);
		User newUser = new User("Guilherme", "email@test.com", "encodedPassword", UserRoles.COSTUMER);
		String token = "testToken";

		when(userService.findByLogin(createUserDTO.email())).thenReturn(null);
		// O saveUser recebe CreateUserDTO, não um User
		doNothing().when(userService).saveUser(any(CreateUserDTO.class));
		when(tokenService.generateToken(any(User.class))).thenReturn(token);

		Authentication authentication = mock(Authentication.class);
		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenReturn(authentication);
		when(authentication.getPrincipal()).thenReturn(newUser);

		RegisterRespondeDTO result = authenticationService.register(createUserDTO);

		assertNotNull(result);
		assertEquals(token, result.token());

		verify(userService).findByLogin(createUserDTO.email());
		// Verificar que saveUser foi chamado com CreateUserDTO
		verify(userService).saveUser(any(CreateUserDTO.class));
		verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
		verify(tokenService).generateToken(any(User.class));
	}

	@Test
	@DisplayName("Should throw exception when registering a user with an already registered email")
	void registerWithExistingUserThrowsException() {
		CreateUserDTO createUserDTO = new CreateUserDTO(
				"Jane Doe", "janedoe@example.com", "password123", UserRoles.COSTUMER, null);
		User existingUser = new User("Jane Doe", "janedoe@example.com", "encodedPassword123", UserRoles.COSTUMER);

		when(userService.findByLogin(createUserDTO.email())).thenReturn(existingUser);

		assertThrows(IllegalArgumentException.class,
				() -> authenticationService.register(createUserDTO));

		// saveUser não deve ser chamado – ajuste do matcher
		verify(userService, never()).saveUser(any(CreateUserDTO.class));
		verify(authenticationManager, never()).authenticate(any(UsernamePasswordAuthenticationToken.class));
		verify(tokenService, never()).generateToken(any(User.class));
	}

	@Test
	@DisplayName("Should login successfully with valid credentials")
	void loginSuccessfully() {
		User user = new User("Guilherme", "email@test.com", "encryptedPassword", UserRoles.COSTUMER);
		String token = "testToken";
		Authentication authentication = mock(Authentication.class);
		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenReturn(authentication);
		when(authentication.getPrincipal()).thenReturn(user);
		when(tokenService.generateToken(user)).thenReturn(token);

		var loginResponse = authenticationService.login(new AuthenticationDTO("email@test.com", "123456"));

		assertNotNull(loginResponse);
		assertEquals(token, loginResponse.token());
		verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
		verify(tokenService).generateToken(user);
	}

	@Test
	@DisplayName("Should throw exception when login fails due to invalid credentials")
	void loginWithInvalidCredentialsThrowsException() {
		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenThrow(new AuthenticationException("Bad credentials") {});

		assertThrows(AuthenticationException.class, () ->
				authenticationService.login(new AuthenticationDTO("email@test.com", "wrongpassword")));

		verify(tokenService, never()).generateToken(any(User.class));
	}

	@Test
	@DisplayName("Should logout successfully and blacklist token")
	void logoutSuccessfully() {
		jakarta.servlet.http.HttpServletRequest request = mock(jakarta.servlet.http.HttpServletRequest.class);
		when(tokenService.recoverToken(request)).thenReturn("tokenToBlacklist");
		doNothing().when(tokenService).blackListToken("tokenToBlacklist");
		when(request.getRemoteUser()).thenReturn("email@test.com");

		authenticationService.logout(request);

		verify(tokenService).recoverToken(request);
		verify(tokenService).blackListToken("tokenToBlacklist");
	}
}