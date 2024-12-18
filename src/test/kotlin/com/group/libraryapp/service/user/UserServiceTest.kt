package com.group.libraryapp.service.user

import com.group.libraryapp.domain.user.User
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.dto.user.request.UserCreateRequest
import com.group.libraryapp.dto.user.request.UserUpdateRequest
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class UserServiceTest @Autowired constructor(
    private val userRepository: UserRepository,
    private val userService: UserService
) {

    @AfterEach
    fun clean() {
        userRepository.deleteAll()
    }

    @Test
    @DisplayName("유저 저장이 정상 동작한다")
    fun saveUserTest() {
        // given
        val request = UserCreateRequest("치비", null)

        // when
        userService.saveUser(request)

        // then
        val results = userRepository.findAll()
        assertThat(results).hasSize(1)
        assertThat(results[0].name).isEqualTo("치비")
        assertThat(results[0].age).isNull()
    }

    @Test
    @DisplayName("유저 조회가 정상 동작한다")
    fun getUserTest() {
        // given
        userRepository.saveAll(listOf(
            User("제제", 15),
            User("솜이", 6)
        ))

        // when
        val results = userService.getUsers()

        // then
        assertThat(results).hasSize(2)
        assertThat(results).extracting("name").containsExactlyInAnyOrder("제제", "솜이")
        assertThat(results).extracting("age").containsExactlyInAnyOrder(15, 6)
    }

    @Test
    @DisplayName("유저 수정이 정상 동작한다")
    fun updateUserNameTest() {
        // given
        val savedUser = userRepository.save(User("강아지", null))
        val request = UserUpdateRequest(savedUser.id, "아지")

        // when
        userService.updateUserName(request)

        // then
        val result = userRepository.findAll()[0]
        assertThat(result.name).isEqualTo("아지")
    }

    @Test
    @DisplayName("유저 삭제가 정상 동작한다")
    fun deleteUserTest() {
        // given
        userRepository.save(User("벌레", null))

        // when
        userService.deleteUser("벌레")

        // then
        assertThat(userRepository.findAll()).isEmpty()
    }
}