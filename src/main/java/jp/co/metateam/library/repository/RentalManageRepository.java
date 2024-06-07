package jp.co.metateam.library.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.boot.autoconfigure.web.ServerProperties.Jetty.Accesslog.FORMAT;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.service.StockService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


@Repository
public interface RentalManageRepository extends JpaRepository<RentalManage, Long> {
        List<RentalManage> findAll();
 
        Optional<RentalManage> findById(Long id);
 
        @Query("SELECT rm FROM RentalManage rm" +
                " WHERE(rm.status = 0 OR rm.status = 1)" +
                " AND rm.stock.id = ?1" +
                " AND rm.id <> ?2")
 
        List<RentalManage> findByStockIdAndStatusIn1(String StockId, Long rentalId);
 
        @Query("SELECT rm FROM RentalManage rm" +
                " WHERE(rm.status = 0 OR rm.status = 1)" +
                " AND rm.stock.id = ?1")
 
        List<RentalManage> findByStockIdAndStatusIn2(String StockId);

        @Query("SELECT st.id "
                + " from Stock st"
                + " join bookMst bm on st.bookMst.id = bm.id"
                + " left join RentalManage rm on st.id = rm.stock.id"
                + " where(st.bookMst.id = ?1 and rm.expectedRentalOn >= ?2 or ?2 >= rm.expectedReturnOn)"
                + " and st.status in (0)")
                //"FROM Stock st " +
                //"INNER JOIN st.bookMst bm " +
                //"INNER JOIN RentalManage rm ON rm.stock.id = st.id " +
                //"WHERE (bm.id = ?1 AND rm.expectedRentalOn >= ?2 AND ?2 >= rm.expectedReturnOn) " +
                //"AND st.status = 0")

        List<String> findByAvailableStockId(String bookId, Date date);

}