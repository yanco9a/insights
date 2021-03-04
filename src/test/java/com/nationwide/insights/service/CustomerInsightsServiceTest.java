package com.nationwide.insights.service;

import com.nationwide.insights.api.exception.TransactionNotFoundException;
import com.nationwide.insights.domain.Insight;
import com.nationwide.insights.domain.customer.Customer;
import com.nationwide.insights.domain.transactions.TransactionRepository;
import com.nationwide.insights.domain.transactions.Transactions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static com.nationwide.insights.service.TransactionCategory.BILL;
import static com.nationwide.insights.service.TransactionCategory.CAFES;
import static com.nationwide.insights.service.TransactionCategory.RESTAURANTS;
import static java.lang.String.format;
import static java.math.BigDecimal.valueOf;
import static java.time.LocalDate.now;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-local.properties")
@ActiveProfiles("test")
public class CustomerInsightsServiceTest {

    @Autowired
    CustomerInsightsService service;

    @MockBean
    private TransactionRepository repository;
    private LocalDate lastMonth;
    private LocalDate thisMonth;

    @BeforeEach
    public void setup() {
        thisMonth = now();
        lastMonth = now().minusMonths(1L);
    }

    @DisplayName("all transactions are the same category, but vendors differ returns one spendByCategory insight")
    @Test
    public void spendByCategoryInsightWhenCategoriesTheSameButVendorsDifferTest() {
        // Given
        Customer customer = new Customer(1L, "yanny");
        long customerId = customer.getId();
        String sameCategory = RESTAURANTS.toString();
        Transactions restaurantOne = new Transactions(1L,
                valueOf(15000L, 2),
                thisMonth,
                "",
                sameCategory, // And
                "Bob's Burgers", // And
                customer);
        Transactions restaurantTwo = new Transactions(2L,
                valueOf(15000L, 2),
                thisMonth,
                "",
                sameCategory, // And
                "Bob's Burgers", // And
                customer);
        Transactions restaurantThree = new Transactions(3L,
                valueOf(5000L, 2),
                thisMonth,
                "Another restaurant",
                sameCategory,
                "Another restaurant", // And
                customer);
        doReturn(asList(restaurantOne, restaurantTwo, restaurantThree)).when(repository).findAllByCustomerId(customerId);

        // When
        List<Insight> spendingInsights = service.customerInsightsById(customerId);

        // Then
        assertEquals(asList(new Insight("You've spent £350 in restaurants this month")), spendingInsights);
    }

    @DisplayName("no transactions this month returns no insights")
    @Test
    public void noInsightsWhenNoTransactionsThisMthTest() {
        // Given
        Customer customer = new Customer(1L, "yanny");
        long customerId = customer.getId();
        String blah = "blah";

        Transactions irrelevant = new Transactions(1L,
                valueOf(15000L, 2),
                lastMonth, // And
                blah,
                blah,
                blah,
                customer);

        doReturn(asList(irrelevant)).when(repository).findAllByCustomerId(customerId);

        // When
        List<Insight> spendingInsights = service.customerInsightsById(customerId);

        // Then
        assertEquals(emptyList(), spendingInsights);
    }

    @DisplayName("transactions in two different categories returns two different spendByCategory insights")
    @Test
    public void twoSpendByCategoryInsightsReturnedWhenTwoDifferentCategoriesTest() {
        // Given
        Customer customer = new Customer(1L, "yanny");
        long customerId = customer.getId();

        String sameCategory = RESTAURANTS.toString();
        String sameDescription = "";
        Transactions restaurantOne = new Transactions(1L,
                valueOf(15000L, 2),
                thisMonth,
                sameDescription,
                sameCategory, // And
                "Bob's Burgers",
                customer);
        Transactions restaurantTwo = new Transactions(2L,
                valueOf(15000L, 2),
                thisMonth,
                sameDescription,
                sameCategory, // And
                sameDescription,
                customer);
        Transactions cafeOne = new Transactions(3L,
                valueOf(5000L, 2),
                thisMonth,
                sameDescription,
                CAFES.toString(), // And
                "new vendor",
                customer);
        doReturn(asList(restaurantOne, restaurantTwo, cafeOne)).when(repository).findAllByCustomerId(customerId);

        // When
        List<Insight> spendingInsights = service.customerInsightsById(customerId);

        // Then
        assertEquals(2, spendingInsights.size());
        assertTrue(asList(
                new Insight("You've spent £50 in cafes this month"),
                new Insight("You've spent £300 in restaurants this month"))
                .containsAll(spendingInsights));
    }

    @DisplayName("this month's bill is lower than the last month returns spent LESS insight")
    @Test
    public void billTrackingInsightWhenBillLowerThanLastMthTest() {
        // Given
        BigDecimal higherAmount = valueOf(10000L, 2);
        BigDecimal lowerAmount = valueOf(6000L, 2);
        Customer customer = new Customer(1L, "yanny");
        long customerId = customer.getId();
        String sameDescription = "someDescription";
        String sameVendor = "Vodafone";
        String sameCategory = BILL.toString();
        Transactions lastMonthsBill = new Transactions(1L,
                higherAmount, // And
                lastMonth, // And
                sameDescription,
                sameCategory,
                sameVendor,
                customer);
        Transactions latestLowerBill = new Transactions(2L,
                lowerAmount, // And
                thisMonth, // And
                sameDescription,
                sameCategory,
                sameVendor,
                customer);

        doReturn(asList(lastMonthsBill, latestLowerBill)).when(repository).findAllByCustomerId(customerId);

        // When
        List<Insight> spendingInsights = service.customerInsightsById(customerId);

        // Then
        assertEquals(
                asList(new Insight("Your latest Vodafone bill is £40 less than previous months")), spendingInsights);
    }

    @DisplayName("this month's bill is lower than 2 previous months returns spent LESS insight")
    @Test
    public void billTrackingInsightWhenBillLowerThanPreviousMthsTest() {
        // Given
        BigDecimal higherAmount = valueOf(10000L, 2);
        BigDecimal lowerAmount = valueOf(6000L, 2);
        Customer customer = new Customer(1L, "yanny");
        long customerId = customer.getId();
        String sameVendor = "Vodafone";
        String sameCategory = BILL.toString();
        String sameDescription = "someDescription";
        Transactions twoMonthsAgo = new Transactions(1L,
                higherAmount, // And
                lastMonth.minusMonths(1), // And
                sameDescription,
                sameCategory,
                sameVendor,
                customer);
        Transactions oneMonthAgo = new Transactions(2L,
                higherAmount,
                lastMonth,
                sameDescription,
                sameCategory,
                sameVendor,
                customer);
        Transactions latestLowerBill = new Transactions(3L,
                lowerAmount,
                thisMonth,
                sameDescription,
                sameCategory,
                sameVendor,
                customer);

        doReturn(asList(twoMonthsAgo, oneMonthAgo, latestLowerBill)).when(repository).findAllByCustomerId(customerId);

        // When
        List<Insight> spendingInsights = service.customerInsightsById(customerId);

        // Then
        assertEquals(asList(
                new Insight("Your latest Vodafone bill is £40 less than previous months")), spendingInsights);
    }

    @DisplayName("this month's bill is higher than the last month returns a spending MORE insight")
    @Test
    public void billTrackingInsightWhenBillHigherThanLastMthTest() {
        // Given
        BigDecimal higherAmount = valueOf(10000L, 2);
        BigDecimal lowerAmount = valueOf(6000L, 2);
        Customer customer = new Customer(1L, "yanny");
        long customerId = customer.getId();
        String sameVendor = "Vodafone";
        String blah = "blah";
        Transactions bill = new Transactions(1L,
                lowerAmount,
                lastMonth,
                blah,
                BILL.toString(),
                sameVendor,
                customer);
        Transactions latestHigherBill = new Transactions(2L,
                higherAmount,
                thisMonth,
                blah,
                BILL.toString(),
                sameVendor,
                customer);

        doReturn(asList(latestHigherBill, bill)).when(repository).findAllByCustomerId(customerId);

        // When
        List<Insight> spendingInsights = service.customerInsightsById(customerId);

        // Then
        assertEquals(asList(
                new Insight("Your latest Vodafone bill is £40 more than previous months")), spendingInsights);
    }

    @DisplayName("this month's bill is higher than 2 previous months returns spent MORE insight")
    @Test
    public void billTrackingInsightWhenBillHigherThanPreviousMthsTest() {
        // Given
        BigDecimal higherAmount = valueOf(10000L, 2);
        BigDecimal lowerAmount = valueOf(6000L, 2);
        Customer customer = new Customer(1L, "yanny");
        long customerId = customer.getId();
        String sameVendor = "Vodafone";
        String sameCategory = BILL.toString();
        String sameDescription = "someDescription";
        Transactions twoMonthsAgo = new Transactions(1L,
                lowerAmount, // And
                lastMonth.minusMonths(1), // And
                sameDescription,
                sameCategory,
                sameVendor,
                customer);
        Transactions oneMonthAgo = new Transactions(2L,
                lowerAmount,
                lastMonth,
                sameDescription,
                sameCategory,
                sameVendor,
                customer);
        Transactions latestLowerBill = new Transactions(3L,
                higherAmount,
                thisMonth,
                sameDescription,
                sameCategory,
                sameVendor,
                customer);

        doReturn(asList(twoMonthsAgo, oneMonthAgo, latestLowerBill)).when(repository).findAllByCustomerId(customerId);

        // When
        List<Insight> spendingInsights = service.customerInsightsById(customerId);

        // Then
        assertEquals(asList(
                new Insight("Your latest Vodafone bill is £40 more than previous months")), spendingInsights);
    }

    @DisplayName("this month, spent MORE on bills, LESS on food, and spendByCategory returns three insights")
    @Test
    public void billTrackingInsightWhenSomeBillsHigherSomeBillsLowerThanLastMthTest() {
        // Given
        BigDecimal higherAmount = valueOf(10000L, 2);
        BigDecimal lowerAmount = valueOf(6000L, 2);
        Customer customer = new Customer(1L, "yanny");
        long customerId = customer.getId();
        String blah = "blah";
        // And
        String categoryBill = BILL.toString();
        String vodafone = "Vodafone";
        Transactions bill = new Transactions(1L,
                lowerAmount,
                lastMonth,
                blah,
                categoryBill,
                vodafone,
                customer);
        Transactions thisMonthsBillHigher = new Transactions(3L,
                higherAmount,
                thisMonth,
                blah,
                categoryBill,
                vodafone,
                customer);
        // And
        String categoryRestaurants = RESTAURANTS.toString();
        String nandos = "Nandos'";
        Transactions food = new Transactions(2L,
                higherAmount,
                lastMonth,
                blah,
                categoryRestaurants,
                nandos,
                customer);
        Transactions thisMonthsFoodLower = new Transactions(4L,
                lowerAmount,
                thisMonth,
                blah,
                categoryRestaurants,
                nandos,
                customer);


        doReturn(asList(
                bill, food, thisMonthsBillHigher, thisMonthsFoodLower)).when(repository).findAllByCustomerId(customerId);

        // When
        List<Insight> spendingInsights = service.customerInsightsById(customerId);

        // Then
        assertEquals(asList(
                new Insight("Your latest Vodafone bill is £40 more than previous months"),
                new Insight("Your latest restaurants spend in Nandos' is £40 less than previous months"),
                new Insight("You've spent £60 in restaurants this month")), spendingInsights);
    }

    @DisplayName("spent the same amount on vodafone bill this month and last month, returns no insight")
    @Test
    public void noInsightsWhenLastMonthAndThisMthTheSameSpentTest() {
        // Given
        Customer customer = new Customer(1L, "yanny");
        long customerId = customer.getId();
        String vodafone = "Vodafone";
        BigDecimal sameAmount = valueOf(200L);
        Transactions vodafoneOne = new Transactions(1L,
                sameAmount, // And
                thisMonth,
                "",
                BILL.toString(),
                vodafone,
                customer);
        Transactions vodafoneTwo = new Transactions(2L,
                sameAmount, // And
                lastMonth,
                "",
                BILL.toString(),
                vodafone,
                customer);

        doReturn(asList(vodafoneOne, vodafoneTwo))
                .when(repository).findAllByCustomerId(customerId);

        // When
        List<Insight> spendingInsights = service.customerInsightsById(customerId);

        // Then
        assertEquals(emptyList(), spendingInsights);
    }

    @DisplayName("spent the same amount on vodafone bill this month and last month, returns no insight")
    @Test
    public void noInsightsWhenPreviousMonthsAndThisMthTheSameSpentTest() {
        // Given
        Customer customer = new Customer(1L, "yanny");
        long customerId = customer.getId();
        String vodafone = "Vodafone";
        BigDecimal sameAmount = valueOf(200L);
        String sameCategory = BILL.toString();
        String blah = "";
        Transactions twoMonthAgo = new Transactions(1L,
                sameAmount, // And
                thisMonth,
                blah,
                sameCategory,
                vodafone,
                customer);
        Transactions oneMonthAgo = new Transactions(2L,
                sameAmount, // And
                lastMonth,
                blah,
                sameCategory,
                vodafone,
                customer);
        Transactions thisMonthSame = new Transactions(3L,
                sameAmount, // And
                thisMonth,
                blah,
                sameCategory,
                vodafone,
                customer);

        doReturn(asList(oneMonthAgo, twoMonthAgo, thisMonthSame))
                .when(repository).findAllByCustomerId(customerId);

        // When
        List<Insight> spendingInsights = service.customerInsightsById(customerId);

        // Then
        assertEquals(emptyList(), spendingInsights);
    }

    @DisplayName("spent the same last month as this month only returns insight on relevant transaction")
    @Test
    public void multipleVendorsBillsButOnlyRelevantSpendByCategoryInsightThisMthTest() {
        // Given
        BigDecimal amount = valueOf(10000L, 2);
        Customer customer = new Customer(1L, "yanny");
        long customerId = customer.getId();
        Transactions irrelevantOne = new Transactions(1L,
                amount,
                lastMonth,
                "a",
                BILL.toString(),
                "a",
                customer);
        Transactions irrelevantTwo = new Transactions(2L,
                amount,
                thisMonth,
                "b",
                BILL.toString(),
                "b",
                customer);
        // And
        Transactions relevantTransaction = new Transactions(3L,
                valueOf(200L, 2),
                thisMonth,
                "relevant",
                RESTAURANTS.toString(),
                "relevant",
                customer);

        doReturn(asList(relevantTransaction, irrelevantOne, irrelevantTwo))
                .when(repository).findAllByCustomerId(customerId);

        // When
        List<Insight> spendingInsights = service.customerInsightsById(customerId);

        // Then
        assertEquals(asList(new Insight("You've spent £2 in restaurants this month")), spendingInsights);
    }

    @DisplayName("two previous bill £50 each, latest bill £100. customer spent £50 more this month")
    @Test
    public void averageCostOfTwoLowerPreviousBillsMinusThisMonthsHigherBillTest() {
        // Given
        Customer customer = new Customer(1L, "yanny");
        long customerId = customer.getId();
        String vodafone = "Vodafone";
        String blah = "blah";
        String sameCategory = BILL.toString();

        Transactions twoMonthsAgo = new Transactions(1L,
                valueOf(5000L, 2), // And
                lastMonth.minusMonths(1),
                blah,
                sameCategory,
                vodafone,
                customer);
        Transactions oneMonthAgo = new Transactions(2L,
                valueOf(5000L, 2), // And
                lastMonth,
                blah,
                sameCategory,
                vodafone,
                customer);
        Transactions thisMonthBill = new Transactions(3L,
                valueOf(10000L, 2), // And
                thisMonth,
                blah,
                sameCategory,
                vodafone,
                customer);

        doReturn(asList(twoMonthsAgo, oneMonthAgo, thisMonthBill)).when(repository).findAllByCustomerId(customerId);

        // When
        List<Insight> spendingInsights = service.customerInsightsById(customerId);

        // Then
        assertEquals(asList(new Insight("Your latest Vodafone bill is £50 more than previous months")), spendingInsights);
    }

    @DisplayName("transaction not found when customer with requested id not found")
    @Test
    public void transactionNotFoundTest() {
        // Given
        long customerDoesNotExist = 99L;

        // When
        TransactionNotFoundException exception = assertThrows(TransactionNotFoundException.class, () ->
                service.customerInsightsById(customerDoesNotExist));

        assertEquals(format("Customer with id %d not found", customerDoesNotExist), exception.getMessage());
    }
}