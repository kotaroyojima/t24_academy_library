package jp.co.metateam.library.service;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.co.metateam.library.constants.Constants;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.BookMstDto;
import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.model.RentalManageDto;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.model.StockDto;
import jp.co.metateam.library.repository.BookMstRepository;
import jp.co.metateam.library.repository.RentalManageRepository;
import jp.co.metateam.library.repository.StockRepository;

@Service
public class StockService {
    private final BookMstRepository bookMstRepository;
    private final StockRepository stockRepository;
    private final RentalManageRepository rentalManageRepository;

    @Autowired
    public StockService(BookMstRepository bookMstRepository, StockRepository stockRepository, RentalManageRepository rentalManageRepository){
        this.bookMstRepository = bookMstRepository;
        this.stockRepository = stockRepository;
        this.rentalManageRepository = rentalManageRepository;
    }

    @Transactional
    public List<Stock> findAll() {
        List<Stock> stocks = this.stockRepository.findByDeletedAtIsNull();

        return stocks;
    }
    
    @Transactional
    public List <Stock> findStockAvailableAll() {
        List <Stock> stocks = this.stockRepository.findByDeletedAtIsNullAndStatus(Constants.STOCK_AVAILABLE);

        return stocks;
    }

    @Transactional
    public Stock findById(String id) {
        return this.stockRepository.findById(id).orElse(null);
    }

    @Transactional
    public int countByStockIdAndStatusIn(Long book_id){
        int availableStockCount = this.stockRepository.countByStockIdAndStatusIn(book_id,Constants.STOCK_AVAILABLE);
        int unAvailableStockCount = this.stockRepository.countByStockIdAndStatusIn(book_id,Constants.STOCK_UNAVAILABLE);
        Integer stockCount = availableStockCount + unAvailableStockCount;
        return stockCount;
    }

    @Transactional
    public int countDatesBetweenRentalAndReturn(Long bookId, Date date){
       int rentalAvailableCount = this.stockRepository.countDatesBetweenRentalAndReturn(bookId, date);
       return rentalAvailableCount;
    }

    @Transactional
    public List<String> findByAvailableStockId(String bookId, Date date){
        List<String> AvailableStockId = this.rentalManageRepository.findByAvailableStockId(bookId, date);
        return AvailableStockId;
    }

    @Transactional 
    public void save(StockDto stockDto) throws Exception {
        try {
            Stock stock = new Stock();
            BookMst bookMst = this.bookMstRepository.findById(stockDto.getBookId()).orElse(null);
            if (bookMst == null) {
                throw new Exception("BookMst record not found.");
            }

            stock.setBookMst(bookMst);
            stock.setId(stockDto.getId());
            stock.setStatus(stockDto.getStatus());
            stock.setPrice(stockDto.getPrice());

            // データベースへの保存
            this.stockRepository.save(stock);
        } catch (Exception e) {
            throw e;
        }
    }

    @Transactional 
    public void update(String id, StockDto stockDto) throws Exception {
        try {
            Stock stock = findById(id);
            if (stock == null) {
                throw new Exception("Stock record not found.");
            }

            BookMst bookMst = stock.getBookMst();
            if (bookMst == null) {
                throw new Exception("BookMst record not found.");
            }

            stock.setId(stockDto.getId());
            stock.setBookMst(bookMst);
            stock.setStatus(stockDto.getStatus());
            stock.setPrice(stockDto.getPrice());

            // データベースへの保存
            this.stockRepository.save(stock);
        } catch (Exception e) {
            throw e;
        }
    }

    public List<Object> generateDaysOfWeek(int year, int month, LocalDate startDate, int daysInMonth) {
        List<Object> daysOfWeek = new ArrayList<>();
        for (int dayOfMonth = 1; dayOfMonth <= daysInMonth; dayOfMonth++) {
            LocalDate date = LocalDate.of(year, month, dayOfMonth);
            DateTimeFormatter formmater = DateTimeFormatter.ofPattern("dd(E)", Locale.JAPANESE);
            daysOfWeek.add(date.format(formmater));
        }

        return daysOfWeek;
    }

    public String[][] generateValues(Integer year, Integer month, Integer daysInMonth) {
        Stock stock = new Stock();
        Calendar calendar = Calendar.getInstance();//calendarを呼び出している
        calendar.clear();
        calendar.set(year,month-1,daysInMonth,0,0,0);//month-1っちいうのはこいつの性質、これで正確な時間のカレンダーがセットされる
        Date date = calendar.getTime();
        List<BookMst> books = bookMstRepository.findAll();
        List<String> AvailableId = findByAvailableStockId(stock.getId(),date);
        int bookNum = books.size();
        String [][] bookCalendar = new String[bookNum][daysInMonth+3];
    
        for (int i = 0; i < books.size(); i++) {
            BookMst book = books.get(i);
            String bookTitle = book.getTitle(); // 対象の書籍名
            int stockCount = countByStockIdAndStatusIn(book.getId()); // 対象書籍の在庫総数
            bookCalendar[i][0] = bookTitle;
            bookCalendar[i][1] = String.valueOf(stockCount);
            bookCalendar[i][2] = AvailableId.get(i);

            

            for (int j = 3; j < daysInMonth+3; j++) {
                calendar.set(year,month-1,j-2,0,0,0);//month-1っちいうのはこいつの性質、j-1はdaysInMonthで+2しちゃってるから、これで正確な時間のカレンダーがセットされる
                Date day = calendar.getTime();
                int rentalAvailableCount = countDatesBetweenRentalAndReturn(book.getId(),day); 
                // 在庫総数から貸出数を引く
                int remainingStockCount = stockCount - rentalAvailableCount;
                bookCalendar[i][j] = String.valueOf(remainingStockCount);
            }
        }
    
        return bookCalendar;
    }
    
}    