
对应的设计文档参见：https://www.jianshu.com/p/92c6f402b543

<h1>背景</h1>

<p>   前段时间在做应用的性能优化时，分析了下整体请求，profile看到90%的时间更多的是一些外部服务的I/O等待，cpu利用率其实不高，在10%以下。 单次请求的响应时间在50ms左右，所以tps也不会太高，测试环境压力测试过程，受限于环境因素撑死只能到200tps，20并发下。</p>

<p> </p>
<h1>I/O</h1>
<div>目前一般的I/O的访问速度： L1 &gt; L2 &gt; memory -&gt; disk or network</div>
<div> </div>
<div>常见的IO： </div>

<div><ol>
<li>nas上文件 (共享文件存储)</li>
<li>output/xxx (磁盘文件)</li>
<li>memcache client /  cat client  (cache服务)</li>
<li>database (oracle , mysql)  (数据库)</li>
<li>dubbo client  (外部服务)</li>
<li>search client (搜索引擎)</li>
</ol></div>

<p><span style="font-size: 24px; font-weight: bold;">思路</span></p>

<p> </p>
<p>正因为考虑到I/O阻塞，长的外部环境单个请求处理基本都是在几十ms，最终的出路只能异步+并行，从而诞生了该开源产品</P>

<h1>项目介绍</h1>
<p>名称：asyncload </p>
<p>译意： async cocurrent load</p>
<p>语言： 纯java开发</p>
<p>定位： 业务层异步并行加载工具包，减少页面响应时间</p>
<p> </p>

<h1>工作原理</h1>
<p><br><img src="http://dl.iteye.com/upload/attachment/423274/bc5877e7-a673-32e3-b8d9-e4f8236d8f11.png" alt=""></p>
<p>原理描述：</p>
<p>1.   针对方法调用，基于字节码增强技术，运行时生成代理类，快速返回mock对象，后台异步进行调用</p>
<p>2.   通过管理和调度线程池，将后台异步调用进行加速处理，达到一个平衡点</p>
<p>3.   业务执行过程需要获取mock对象的真实数据时，阻塞等待原始结果返回，整个过程透明完成</p>

<p>很明显，经过异步并行加载后，一次request请求总的响应时间就等于最长的依赖关系请求链的相应时间。 </p>

<h1>相关文档</h1>

See the wiki page for : <a href="https://github.com/alibaba/canal/wiki" >wiki文档</href>

<br/><a name="table-of-contents" class="anchor" href="#table-of-contents">
<span class="mini-icon mini-icon-link"></span></a>wiki文档列表</h3>
<ul>
<li><a class="internal present" href="https://github.com/alibaba/asyncload/wiki/Home">Home</a></li>
<li><a class="internal present" href="https://github.com/alibaba/asyncload/wiki/Introduction">Introduction</a></li>
<li><a class="internal present" href="https://github.com/alibaba/asyncload/wiki/QuickStart">QuickStart</a></li>
<li><a class="internal present" href="https://github.com/alibaba/asyncload/wiki/ChangeLog">ChangeLog</a></li>
<li><a class="internal present" href="https://github.com/alibaba/asyncload/wiki/用户手册">用户手册</a></li>
</ul>

<p> </p>
<h1>问题反馈</h1>
<p>1. qq交流群： 161559791</span></p>
<p>2. 邮件交流： jianghang115@gmail.com</span></p>
<p>3. 新浪微博： agapple0002</span></p>
<p>4. 报告issue：</span><a href="https://github.com/alibaba/asyncload/issues">issues</a></p>
