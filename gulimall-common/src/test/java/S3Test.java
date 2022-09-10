import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.atguigu.common.config.S3Config;
import org.junit.jupiter.api.Test;

public class S3Test {
    @Test
    void S3tet(){
        AmazonS3 client = S3Config.client();
        Bucket demo = client.createBucket("demo");
        System.out.println(demo);
    }
}
