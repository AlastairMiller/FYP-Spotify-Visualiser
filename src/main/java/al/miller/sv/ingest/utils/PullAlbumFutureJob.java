package al.miller.sv.ingest.utils;

import com.wrapper.spotify.model_objects.specification.Album;
import lombok.AllArgsConstructor;
import refinedDataModels.RefinedAlbum;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;

@AllArgsConstructor
public class PullAlbumFutureJob implements Runnable {
    private SynchronousQueue<Future<Album>> futures;
    private Album finishedAlbum;
    private List<Future<?>> futuresToReturn;

    @Override
    public void run() {
        while (futures.size() > 0) {
            Future<Album> nextInQueue = futures.remove();
            if(nextInQueue.isDone()){
                try {
                    finishedAlbum = nextInQueue.get();
                    RefinedAlbum refinedAlbum = DownstreamMapper.mapAlbum(finishedAlbum);
                    refinedAlbum.getArtistsIds();
                }catch (Exception e){
                    //retry request
                }

            }else {
                futures.add(nextInQueue);
            }
        }
    }


}
