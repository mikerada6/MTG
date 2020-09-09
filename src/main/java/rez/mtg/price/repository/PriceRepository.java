package rez.mtg.price.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rez.mtg.price.magic.Card;
import rez.mtg.price.magic.Price;

import java.sql.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface PriceRepository extends CrudRepository<Price, Long> {
    List<Price> findAll();

    Set<Price> findAllByCardId(String cardId);

    Set<Price> findAllByDate(Date date);

    Optional<Price> findByDateAndAndCard(Date date, Card card);

    @Query(value = "select * from price where card_id= :cardId order by date desc limit 1;", nativeQuery = true )
    public Optional<Price> findMostRecentByCard(@Param("cardId") String cardId);

    @Query(value = "select * from price where card_id= :cardId order by date desc limit 7;", nativeQuery = true )
    public List<Price> findMostRecentWeekByCard(@Param("cardId") String cardId);

    void deleteByIdIn(List<Long> ids);

    //	List<Date> findDistinctDate();

    @Query(value = "SELECT DISTINCT date FROM price", nativeQuery = true)
    List<Date> findAllDate();
}
