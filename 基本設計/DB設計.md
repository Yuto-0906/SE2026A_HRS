# ホテル予約システム DB設計

## 1．方針

システム分析で得られた`HotelUser`，`Room`，`Reservation`を永続化する。空室数は重複して保持せず，登録済みの部屋と宿泊日の割当から計算する。

## 2．テーブル

### USERS

| 列 | 型 | 制約 | 対応する属性 |
|---|---|---|---|
| USER_ID | VARCHAR(50) | PRIMARY KEY | `HotelUser.userId` |
| NAME | VARCHAR(100) | NOT NULL | `HotelUser.name` |
| PHONE | VARCHAR(30) | NOT NULL | `HotelUser.phone` |

### ROOMS

| 列 | 型 | 制約 | 対応する属性 |
|---|---|---|---|
| ROOM_NUMBER | VARCHAR(20) | PRIMARY KEY | `Room.roomNumber` |

### RESERVATIONS

| 列 | 型 | 制約 | 対応する属性 |
|---|---|---|---|
| RESERVATION_NUMBER | VARCHAR(50) | PRIMARY KEY | `Reservation.reservationNumber` |
| USER_ID | VARCHAR(50) | NOT NULL，FOREIGN KEY | `Reservation.user` |
| ROOM_NUMBER | VARCHAR(20) | NOT NULL，FOREIGN KEY | `Reservation.room` |
| RESERVED_AT | TIMESTAMP | NOT NULL | `Reservation.reservedAt` |
| CHECK_IN_DATE | DATE | NOT NULL | `Reservation.checkInDate` |
| STATUS | VARCHAR(20) | NOT NULL | `Reservation.status` |

### ROOM_ALLOCATIONS

| 列 | 型 | 制約 | 説明 |
|---|---|---|---|
| ROOM_NUMBER | VARCHAR(20) | PRIMARY KEYの一部，FOREIGN KEY | 割り当てられた部屋 |
| CHECK_IN_DATE | DATE | PRIMARY KEYの一部 | 宿泊日 |
| RESERVATION_NUMBER | VARCHAR(50) | UNIQUE，FOREIGN KEY | 割当元の予約 |

`ROOM_ALLOCATIONS`の主キーを`ROOM_NUMBER`と`CHECK_IN_DATE`の組とする。同じ部屋を同じ宿泊日に複数予約しようとするとDB制約違反になるため，アプリケーション側の検索と競合した場合にも二重予約を防止できる。

予約キャンセル時には予約履歴を`RESERVATIONS`へ残し，対応する`ROOM_ALLOCATIONS`の行だけを削除する。これにより，同じ部屋を再び予約できる。

## 3．トランザクション

次の操作をそれぞれ一つのトランザクションとして処理する。

- 予約：ユーザ登録または更新，空室検索，予約登録，部屋割当登録
- キャンセル：予約状態更新，部屋割当削除
- チェックイン：予約状態確認，予約状態更新
- チェックアウト：予約状態確認，予約状態更新

例外発生時には全変更をロールバックする。

## 4．既存DBからの変更

- `AVAILABLEQTY`を廃止する。
- `PAYMENT`を廃止する。
- `ROOM.STAYINGDATE`を廃止する。
- `RESERVATION`を`RESERVATIONS`へ置き換え，ユーザ，部屋，予約受付日時を追加する。
- 日付文字列を`DATE`または`TIMESTAMP`として保存する。
