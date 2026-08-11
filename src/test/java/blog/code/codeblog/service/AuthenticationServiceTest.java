package blog.code.codeblog.service;

import blog.code.codeblog.command.user.CreateUserCommand;
import blog.code.codeblog.command.user.LoginCommand;
import blog.code.codeblog.dto.authentication.RegisterResponseDTO;
import blog.code.codeblog.enums.AuthFlow;
import blog.code.codeblog.enums.UserRoles;
import blog.code.codeblog.model.User;
import blog.code.codeblog.service.integration.Auth0ServiceIntergration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

	@InjectMocks
	private AuthenticationService authenticationService;

	@Mock
	private UserService userService;

	@Mock
	private TokenService tokenService;

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private Auth0ServiceIntergration auth0ServiceIntergration;

	@BeforeEach
	@DisplayName("Initial mock setup for AuthenticationServiceTest")
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	private CreateUserCommand buildCreateUserCommand(AuthFlow flow) {
		return CreateUserCommand.builder()
				.name("Guilherme")
				.email("email@test.com")
				.credential("123456")
				.flow(flow)
				.profileImage(null)
				.build();
	}

	@Test
	@DisplayName("Should register a user with PASSWORD flow successfully")
	void registerSuccessfullyPasswordFlow() {
		CreateUserCommand cmd = buildCreateUserCommand(AuthFlow.PASSWORD);
		User newUser = new User("Guilherme", "email@test.com", "encodedPassword", UserRoles.COSTUMER);
		String token = "testToken";

		when(userService.findByLogin(cmd.getEmail())).thenReturn(null);
		when(userService.saveUser(cmd)).thenReturn(newUser);
		when(tokenService.generateToken(newUser)).thenReturn(token);

		RegisterResponseDTO result = authenticationService.register(cmd);

		assertNotNull(result);
		assertEquals(token, result.token());
		verify(userService).findByLogin(cmd.getEmail());
		verify(userService).saveUser(cmd);
		verify(tokenService).generateToken(newUser);
		verify(auth0ServiceIntergration, never()).verifyOTP(any());
	}

	@Test
	@DisplayName("Should register a user with OTP flow verifying OTP first")
	void registerSuccessfullyOtpFlow() {
		CreateUserCommand cmd = buildCreateUserCommand(AuthFlow.OTP);
		User newUser = new User("Guilherme", "email@test.com", "encodedPassword", UserRoles.COSTUMER);
		String token = "testToken";

		when(userService.findByLogin(cmd.getEmail())).thenReturn(null);
		when(auth0ServiceIntergration.verifyOTP(any())).thenReturn(true);
		when(userService.saveUser(cmd)).thenReturn(newUser);
		when(tokenService.generateToken(newUser)).thenReturn(token);

		RegisterResponseDTO result = authenticationService.register(cmd);

		assertNotNull(result);
		assertEquals(token, result.token());
		verify(auth0ServiceIntergration).verifyOTP(any());
		verify(userService).saveUser(cmd);
	}

	@Test
	@DisplayName("Should throw exception when registering a user with an already registered email")
	void registerWithExistingUserThrowsException() {
		CreateUserCommand cmd = buildCreateUserCommand(AuthFlow.PASSWORD);
		User existingUser = new User("Jane Doe", "email@test.com", "encodedPassword", UserRoles.COSTUMER);

		when(userService.findByLogin(cmd.getEmail())).thenReturn(existingUser);

		assertThrows(IllegalArgumentException.class, () -> authenticationService.register(cmd));

		verify(userService, never()).saveUser(any(CreateUserCommand.class));
		verify(authenticationManager, never()).authenticate(any(UsernamePasswordAuthenticationToken.class));
		verify(tokenService, never()).generateToken(any(User.class));
	}

	@Test
	@DisplayName("Should login successfully with PASSWORD flow")
	void loginSuccessfullyPasswordFlow() {
		User user = new User("Guilherme", "email@test.com", "encryptedPassword", UserRoles.COSTUMER);
		String token = "testToken";
		Authentication authentication = mock(Authentication.class);

		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenReturn(authentication);
		when(authentication.getPrincipal()).thenReturn(user);
		when(tokenService.generateToken(user)).thenReturn(token);

		LoginCommand cmd = LoginCommand.builder()
				.login("email@test.com")
				.credential("123456")
				.flow(AuthFlow.PASSWORD)
				.build();

		var loginResponse = authenticationService.login(cmd);

		assertNotNull(loginResponse);
		assertEquals(token, loginResponse.token());
		verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
		verify(tokenService).generateToken(user);
		verify(auth0ServiceIntergration, never()).verifyOTP(any());
	}

	@Test
	@DisplayName("Should login successfully with OTP flow")
	void loginSuccessfullyOtpFlow() {
		User user = new User("Guilherme", "email@test.com", "encryptedPassword", UserRoles.COSTUMER);
		String token = "testToken";

		when(auth0ServiceIntergration.verifyOTP(any())).thenReturn(true);
		when(userService.findByLogin("email@test.com")).thenReturn(user);
		when(tokenService.generateToken(user)).thenReturn(token);

		LoginCommand cmd = LoginCommand.builder()
				.login("email@test.com")
				.credential("123456")
				.flow(AuthFlow.OTP)
				.build();

		var loginResponse = authenticationService.login(cmd);

		assertNotNull(loginResponse);
		assertEquals(token, loginResponse.token());
		verify(auth0ServiceIntergration).verifyOTP(any());
		verify(userService).findByLogin("email@test.com");
		verify(tokenService).generateToken(user);
		verify(authenticationManager, never()).authenticate(any());
	}

	@Test
	@DisplayName("Should throw exception when login fails due to invalid credentials (PASSWORD)")
	void loginWithInvalidCredentialsThrowsException() {
		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenThrow(new AuthenticationException("Bad credentials") {});

		LoginCommand cmd = LoginCommand.builder()
				.login("email@test.com")
				.credential("wrongpassword")
				.flow(AuthFlow.PASSWORD)
				.build();

		assertThrows(AuthenticationException.class, () -> authenticationService.login(cmd));
		verify(tokenService, never()).generateToken(any(User.class));
	}

	@Test
	@DisplayName("Should throw exception when OTP is invalid during login")
	void loginWithInvalidOtpThrowsException() {
		when(auth0ServiceIntergration.verifyOTP(any()))
				.thenThrow(new BadCredentialsException("Invalid or expired OTP"));

		LoginCommand cmd = LoginCommand.builder()
				.login("email@test.com")
				.credential("000000")
				.flow(AuthFlow.OTP)
				.build();

		assertThrows(BadCredentialsException.class, () -> authenticationService.login(cmd));
		verify(tokenService, never()).generateToken(any(User.class));
		verify(userService, never()).findByLogin(any());
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