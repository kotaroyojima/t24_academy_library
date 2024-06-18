package jp.co.metateam.library.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.model.Stock;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    
    List<Stock> findAll();

    List<Stock> findByDeletedAtIsNull();

    List<Stock> findByDeletedAtIsNullAndStatus(Integer status);

	Optional<Stock> findById(String id);
    
    List<Stock> findByBookMstIdAndStatus(Long book_id,Integer status);

    @Query("SELECT COUNT (st) FROM Stock st" +
            " WHERE(st.bookMst.id = ?1 AND st.status = ?2)") 
 
    int countByStockIdAndStatusIn(Long bookId, Integer status);

    @Query("SELECT COUNT (st) FROM Stock st" + 
            " inner join RentalManage rm on rm.stock.id = st.id" +//在庫管理番号をキーにしてRentalManageと内部結合、「RentalManage」はmodelの指定してるやつと一緒じゃないといけない
            " WHERE (st.bookMst.id =?1 AND rm.status in (0,1) AND rm.expectedRentalOn <= ?2 AND ?2 <= rm.expectedReturnOn)")
    int countDatesBetweenRentalAndReturn(Long bookId, Date date);

    


}
