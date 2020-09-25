package rez.mtg.price.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import rez.mtg.price.magic.Card;
import rez.mtg.price.model.Temp;

@Repository
public interface TempRepository extends CrudRepository<Temp, String>
{
}
