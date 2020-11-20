package pp.ua.library.yummybook.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SelectBeforeUpdate;

import javax.persistence.*;

@Entity
@Table(catalog = "library")
@EqualsAndHashCode(of = "id")
@Getter
@Setter
@DynamicUpdate
@DynamicInsert
@SelectBeforeUpdate
public class Vote {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    private int value;

    @Column(name = "book_id")
    private Long bookId;

    private String username;

    public Vote(Long id, int value, Long bookId, String username){
        this.id = id;
        this.value = value;
        this.bookId = bookId;
        this.username = username;
    }

    public Vote(int value, Long bookId, String username){
        this.value = value;
        this.bookId = bookId;
        this.username = username;
    }

    public Vote(){

    }
}