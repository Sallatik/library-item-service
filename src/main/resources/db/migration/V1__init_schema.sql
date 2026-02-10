CREATE TABLE item
(
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    title    VARCHAR(255) NOT NULL,
    category ENUM('NEW', 'CLASSIC', 'STANDARD') NOT NULL
);

CREATE TABLE user_order
(
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type    ENUM('BORROW', 'RETURN') NOT NULL,
    created TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_to_item
(
    user_order_id BIGINT NOT NULL,
    item_id  BIGINT NOT NULL,

    PRIMARY KEY (user_order_id, item_id),
    FOREIGN KEY (user_order_id) REFERENCES user_order (id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES item (id) ON DELETE CASCADE
);

CREATE TABLE currently_borrowed_item
(
    item_id  BIGINT NOT NULL,
    user_id  BIGINT NOT NULL,
    user_order_id BIGINT NOT NULL,

    PRIMARY KEY (item_id),

    FOREIGN KEY (item_id) REFERENCES item (id) ON DELETE CASCADE,
    FOREIGN KEY (user_order_id) REFERENCES user_order (id) ON DELETE CASCADE
);


CREATE TABLE late_fee
(
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT  NOT NULL,
    days    INT     NOT NULL,
    paid    BOOLEAN NOT NULL         DEFAULT FALSE,
    created TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);