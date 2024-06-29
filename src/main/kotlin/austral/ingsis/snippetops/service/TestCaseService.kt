package austral.ingsis.snippetops.service

import austral.ingsis.snippetops.repository.BucketRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class TestCaseService(
    @Autowired val bucketRepository: BucketRepository,
)
