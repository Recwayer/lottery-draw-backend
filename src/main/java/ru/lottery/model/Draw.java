package ru.lottery.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import ru.lottery.model.enums.DrawStatus;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Draw extends BaseEntity {

  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DrawStatus status;

  @Column(name = "draw_date")
  private LocalDateTime drawDate;

  public static Draw create(String name, DrawStatus status, LocalDateTime drawDate) {
    Draw draw = new Draw();
    draw.setName(name);
    draw.setStatus(status);
    draw.setDrawDate(drawDate);
    return draw;
  }
}
