package ru.lottery.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ru.lottery.model.LotteryType;
import ru.lottery.repository.LotteryTypeRepository;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;

@ExtendWith(MockitoExtension.class)
class LotteryTypeServiceTest {

  @Mock LotteryTypeRepository repository;
  @InjectMocks LotteryTypeService service;

  @Test
  void createHappyPath() {
    when(repository.existsByName("Mini")).thenReturn(false);
    when(repository.save(any(LotteryType.class)))
        .thenAnswer(
            inv -> {
              LotteryType t = inv.getArgument(0);
              return t;
            });

    LotteryType result = service.create("Mini", 1, 36, 5);

    assertThat(result.getName()).isEqualTo("Mini");
    assertThat(result.getPoolMin()).isEqualTo(1);
    assertThat(result.getPoolMax()).isEqualTo(36);
    assertThat(result.getPicks()).isEqualTo(5);
  }

  @Test
  void createRejectsBlankName() {
    assertHttpStatus(() -> service.create(null, 1, 49, 6), HttpStatus.BAD_REQUEST);
    assertHttpStatus(() -> service.create("  ", 1, 49, 6), HttpStatus.BAD_REQUEST);
    verify(repository, never()).save(any());
  }

  @Test
  void createRejectsBadPool() {
    assertHttpStatus(() -> service.create("X", 49, 1, 6), HttpStatus.BAD_REQUEST);
    assertHttpStatus(() -> service.create("X", 5, 5, 1), HttpStatus.BAD_REQUEST);
  }

  @Test
  void createRejectsBadPicks() {
    assertHttpStatus(() -> service.create("X", 1, 5, 0), HttpStatus.BAD_REQUEST);
    assertHttpStatus(() -> service.create("X", 1, 5, 6), HttpStatus.BAD_REQUEST);
  }

  @Test
  void createRejectsDuplicateName() {
    when(repository.existsByName("Dup")).thenReturn(true);
    assertHttpStatus(() -> service.create("Dup", 1, 49, 6), HttpStatus.CONFLICT);
  }

  @Test
  void listDelegatesToRepo() {
    LotteryType t = new LotteryType();
    when(repository.findAll()).thenReturn(List.of(t));
    assertThat(service.list()).containsExactly(t);
  }

  @Test
  void getByIdRequiresId() {
    assertHttpStatus(() -> service.getById(null), HttpStatus.BAD_REQUEST);
  }

  @Test
  void getByIdNotFound() {
    UUID id = UUID.randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());
    assertHttpStatus(() -> service.getById(id), HttpStatus.NOT_FOUND);
  }

  @Test
  void getByIdHappyPath() {
    UUID id = UUID.randomUUID();
    LotteryType t = new LotteryType();
    when(repository.findById(id)).thenReturn(Optional.of(t));
    assertThat(service.getById(id)).isSameAs(t);
  }

  private static void assertHttpStatus(Runnable action, HttpStatus status) {
    assertThatThrownBy(action::run)
        .isInstanceOf(HttpStatusException.class)
        .extracting("status")
        .isEqualTo(status);
  }
}
