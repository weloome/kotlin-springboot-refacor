package com.group.libraryapp.service.book

import com.group.libraryapp.domain.book.Book
import com.group.libraryapp.domain.book.BookRepository
import com.group.libraryapp.domain.user.User
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistory
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistoryRepository
import com.group.libraryapp.dto.book.request.BookLoanRequest
import com.group.libraryapp.dto.book.request.BookRequest
import com.group.libraryapp.dto.book.request.BookReturnRequest
import com.group.libraryapp.service.user.UserService
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class BookServiceTest @Autowired constructor(
    private val bookService: BookService,
    private val bookRepository: BookRepository,
    private val userRepository: UserRepository,
    private val userLoanHistoryRepository: UserLoanHistoryRepository
){

    @Autowired
    private lateinit var userService: UserService

    @AfterEach
    fun clean() {
        bookRepository.deleteAll()
        userRepository.deleteAll() // deleteAll()은 자식 테이블까지 찾아서 삭제해준다.
    }

    @Test
    @DisplayName("책 등록이 정상 동작한다")
    fun saveBookTest() {
        // given
        val request = BookRequest("일론 머스크")

        // when
        bookService.saveBook(request)

        // then
        val books = bookRepository.findAll()
        assertThat(books).hasSize(1)
        assertThat(books[0].name).isEqualTo("일론 머스크")
    }

    @Test
    @DisplayName("책 대출이 정상 동작한다")
    fun loanBookTest() {
        // given
        bookRepository.save(Book("코딩 몰라도됩니다"))
        val savedUser = userRepository.save(User("이단비", null))
        val request = BookLoanRequest("이단비", "코딩 몰라도됩니다")

        // when
        bookService.loanBook(request)

        // then
        val results = userLoanHistoryRepository.findAll()
        assertThat(results).hasSize(1)
        assertThat(results[0].bookName).isEqualTo("코딩 몰라도됩니다")
        assertThat(results[0].user.id).isEqualTo(savedUser.id)
        assertThat(results[0].isReturn).isFalse()
    }

    @Test
    @DisplayName("책이 이미 대출되어 있다면, 신규 대출은 실패한다")
    fun loanBookFailTest() {
        // given
        bookRepository.save(Book("코딩 몰라도됩니다"))
        val savedUser = userRepository.save(User("이단비", null))
        userLoanHistoryRepository.save(UserLoanHistory(savedUser, "코딩 몰라도됩니다", false))
        val request = BookLoanRequest("이단비", "코딩 몰라도됩니다")

        // when & then
        val message = assertThrows<IllegalArgumentException> {
            bookService.loanBook(request)
        }.message
        assertThat(message).isEqualTo("진작 대출되어 있는 책입니다")
    }

    @Test
    @DisplayName("책 반납이 정상 동작한다")
    fun returnBookTest() {
        // given
        bookRepository.save(Book("코딩 몰라도됩니다"))
        val savedUser = userRepository.save(User("이단비", null))
        userLoanHistoryRepository.save(UserLoanHistory(savedUser, "코딩 몰라도됩니다", false))
        val request = BookReturnRequest("이단비", "코딩 몰라도됩니다")

        // when
        bookService.returnBook(request)

        // then
        val results = userLoanHistoryRepository.findAll()
        assertThat(results).hasSize(1)
        assertThat(results[0].isReturn).isTrue()
    }
}