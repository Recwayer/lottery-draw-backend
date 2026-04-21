package ru.lottery.model;

import jakarta.persistence.*;

import ru.lottery.model.enums.TicketStatus;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ticket extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "draw_id", nullable = false)
  private Draw draw;

  private String numbers;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TicketStatus status;
}
