package vklab1;

import java.util.UUID;

// Фундаментальні патерни
// Використання Interface: Інтерфейс виступає в ролі контракту класу надаючи простий спосіб доступу до класу з інших класів

interface PaymentStrategy {
    void pay(double amount);
}

// Патерн делегування:
// клас для демонстрації делегування.

class PaymentLogger {
    public void logTransaction(String message) {
        System.out.println("log: " + message);
    }
}

// Породжувальні патерни)
// Singleton: Гарантує, що в системі допускається лише один екземпляр класу

class ConfigurationManager {
    private static ConfigurationManager uniqueInstance;
    private String currency = "UAH";

    private ConfigurationManager() {}

    public static ConfigurationManager instance() {
        if (uniqueInstance == null) {
            uniqueInstance = new ConfigurationManager();
        }
        return uniqueInstance;
    }

    public String getCurrency() {
        return currency;
    }
}

// Поведінкові патерни
// Strategy: Визначає набір інкапсульованих алгоритмів, які можна міняти місцями для виконання певної поведінки

class CreditCardPayment implements PaymentStrategy {
    private String cardNumber;

    public CreditCardPayment(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    @Override
    public void pay(double amount) {
        String currency = ConfigurationManager.instance().getCurrency();
        System.out.println("Знято " + amount + " " + currency + " з картки " + cardNumber);
    }
}

class PayPalPayment implements PaymentStrategy {
    private String email;

    public PayPalPayment(String email) {
        this.email = email;
    }

    @Override
    public void pay(double amount) {
        String currency = ConfigurationManager.instance().getCurrency();
        System.out.println("Знято " + amount + " " + currency + " з PayPal " + email);
    }
}

// Структурні патерни
// Decorator: Дозволяє динамічно обгортати об'єкти для зміни їхніх існуючих обов'язків та поведінки


abstract class PaymentDecorator implements PaymentStrategy {
    protected PaymentStrategy wrappedPayment;

    public PaymentDecorator(PaymentStrategy wrappedPayment) {
        this.wrappedPayment = wrappedPayment;
    }

    @Override
    public void pay(double amount) {
        wrappedPayment.pay(amount);
    }
}

// Конкретний декоратор для додавання комісії до платежу

class FeePaymentDecorator extends PaymentDecorator {
    private double feePercentage;

    public FeePaymentDecorator(PaymentStrategy wrappedPayment, double feePercentage) {
        super(wrappedPayment);
        this.feePercentage = feePercentage;
    }

    @Override
    public void pay(double amount) {
        double fee = amount * (feePercentage / 100);
        double totalAmount = amount + fee;
        System.out.println("Комісія: " + feePercentage + "%. Разом до оплати: " + totalAmount);
        super.pay(totalAmount);
    }
}

// Збірка та використання builder + immutable
// Immutable об'єкт: Задає всі внутрішні значення під час ініціалізації, що дозволяє уникнути ресурсомістких операцій копіювання

class Order {
    private final String orderId;
    private final String customerName;
    private final double totalAmount;

    // Делегуючий клас містить посилання на клас, якому делегується виконання методу

    private final PaymentLogger logger = new PaymentLogger();

    private Order(OrderBuilder builder) {
        this.orderId = builder.orderId;
        this.customerName = builder.customerName;
        this.totalAmount = builder.totalAmount;
    }

    public void processPayment(PaymentStrategy paymentMethod) {
        logger.logTransaction("Початок оплати для замовлення " + orderId + " Клієнт - " + customerName);
        paymentMethod.pay(this.totalAmount);
    }

    // Builder: Дозволяє динамічно створювати об'єкти

    public static class OrderBuilder {
        private String customerName;
        private double totalAmount;
        private String orderId = UUID.randomUUID().toString().substring(0, 8);

        public OrderBuilder(String customerName, double totalAmount) {
            this.customerName = customerName;
            this.totalAmount = totalAmount;
        }

        public Order build() {
            return new Order(this);
        }
    }
}

class FinalEnterpriseDemo {
    public static void main(String[] args) {

        // Перевірка Singleton

        System.out.println("Валюта системи: " + ConfigurationManager.instance().getCurrency() + "\n");

        System.out.println("Сценарій 1: Базова оплата");

        // Створення об'єкта через Builder

        Order order1 = new Order.OrderBuilder("Кіріл", 1500.00).build();

        // Використання Strategy

        PaymentStrategy standardCard = new CreditCardPayment("1234");
        order1.processPayment(standardCard);

        System.out.println("\nСценарій 2: Оплата з накладеною комісією");
        Order order2 = new Order.OrderBuilder("Марія", 2500.00).build();
        PaymentStrategy paypal = new PayPalPayment("kiril@gmail.com");

        // Використання Decorator поверх Strategy

        PaymentStrategy paymentWithFee = new FeePaymentDecorator(paypal, 2.5);
        order2.processPayment(paymentWithFee);
    }
}