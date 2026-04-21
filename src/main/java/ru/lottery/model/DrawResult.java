package ru.lottery.model;

import jakarta.persistence.*;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "draw_result")
@Entity
public class DrawResult extends BaseEntity {

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "draw_id", nullable = false, unique = true)
  private Draw draw;

  @Column(name = "winning_numbers")
  private String winningNumbers;
}
